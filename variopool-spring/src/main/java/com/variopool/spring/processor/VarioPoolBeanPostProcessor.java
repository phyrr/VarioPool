package com.variopool.spring.processor;

import com.variopool.core.executor.VarioPoolExecutor;
import com.variopool.core.executor.VarioPoolRegistry;
import com.variopool.core.model.ThreadPoolConfig;
import com.variopool.core.model.VarioPoolConfig;
import com.variopool.core.queue.ResizableCapacityLinkedBlockingQueue;
import com.variopool.core.support.QueueType;
import com.variopool.core.support.RejectedPolicyType;
import com.variopool.spring.annotation.VarioPoolBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Registers {@link VarioPoolExecutor} beans and applies remote configuration at startup.
 */
public class VarioPoolBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private final VarioPoolConfig properties;
    private ConfigurableListableBeanFactory beanFactory;

    public VarioPoolBeanPostProcessor(VarioPoolConfig properties) {
        this.properties = properties;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableListableBeanFactory configurable) {
            this.beanFactory = configurable;
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof VarioPoolExecutor executor)) {
            return bean;
        }
        VarioPoolBean annotation = findAnnotation(beanName, bean.getClass());
        if (annotation == null) {
            return bean;
        }
        ThreadPoolConfig remote = properties.getExecutors().stream()
                .filter(item -> Objects.equals(item.getPoolId(), executor.getPoolId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Missing variopool.executors config for poolId=" + executor.getPoolId()));

        applyConfig(executor, remote);
        VarioPoolRegistry.register(executor.getPoolId(), executor, remote);
        return bean;
    }

    private VarioPoolBean findAnnotation(String beanName, Class<?> beanClass) {
        VarioPoolBean annotation = AnnotationUtils.findAnnotation(beanClass, VarioPoolBean.class);
        if (annotation != null) {
            return annotation;
        }
        if (beanFactory == null || !beanFactory.containsBeanDefinition(beanName)) {
            return null;
        }
        var beanDefinition = beanFactory.getBeanDefinition(beanName);
        String factoryBeanName = beanDefinition.getFactoryBeanName();
        String factoryMethodName = beanDefinition.getFactoryMethodName();
        if (factoryBeanName != null && factoryMethodName != null) {
            Class<?> factoryClass = beanFactory.getType(factoryBeanName);
            if (factoryClass != null) {
                for (Method method : factoryClass.getDeclaredMethods()) {
                    if (Objects.equals(method.getName(), factoryMethodName)) {
                        VarioPoolBean methodAnnotation = AnnotationUtils.findAnnotation(method, VarioPoolBean.class);
                        if (methodAnnotation != null) {
                            return methodAnnotation;
                        }
                    }
                }
            }
        }
        if (beanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition) {
            Object factoryBean = abstractBeanDefinition.getAttribute("factoryBeanObjectType");
            if (factoryBean instanceof Class<?> factoryClass) {
                for (Method method : factoryClass.getDeclaredMethods()) {
                    VarioPoolBean methodAnnotation = AnnotationUtils.findAnnotation(method, VarioPoolBean.class);
                    if (methodAnnotation != null && Objects.equals(method.getName(), beanName)) {
                        return methodAnnotation;
                    }
                }
            }
        }
        return null;
    }

    private void applyConfig(VarioPoolExecutor executor, ThreadPoolConfig config) {
        if (config.getCorePoolSize() != null && config.getMaximumPoolSize() != null) {
            if (config.getCorePoolSize() > executor.getMaximumPoolSize()) {
                executor.setMaximumPoolSize(config.getMaximumPoolSize());
                executor.setCorePoolSize(config.getCorePoolSize());
            } else {
                executor.setCorePoolSize(config.getCorePoolSize());
                executor.setMaximumPoolSize(config.getMaximumPoolSize());
            }
        }
        if (config.getKeepAliveSeconds() != null) {
            executor.setKeepAliveTime(config.getKeepAliveSeconds(), TimeUnit.SECONDS);
        }
        if (config.getAllowCoreThreadTimeout() != null) {
            executor.allowCoreThreadTimeOut(config.getAllowCoreThreadTimeout());
        }
        if (config.getRejectedHandler() != null) {
            executor.setRejectedExecutionHandler(RejectedPolicyType.create(config.getRejectedHandler()));
        }
        if (config.getWorkQueue() != null) {
            BlockingQueue<Runnable> currentQueue = executor.getQueue();
            if (!Objects.equals(currentQueue.getClass().getSimpleName(), config.getWorkQueue())) {
                BlockingQueue<Runnable> queue = QueueType.createQueue(config.getWorkQueue(), config.getQueueCapacity());
                setWorkQueue(executor, queue);
            } else if (currentQueue instanceof ResizableCapacityLinkedBlockingQueue<?> resizableQueue
                    && config.getQueueCapacity() != null) {
                resizableQueue.setCapacity(config.getQueueCapacity());
            }
        }
    }

    private void setWorkQueue(VarioPoolExecutor executor, BlockingQueue<Runnable> queue) {
        try {
            Field field = java.util.concurrent.ThreadPoolExecutor.class.getDeclaredField("workQueue");
            field.setAccessible(true);
            field.set(executor, queue);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to replace workQueue, add JVM arg: --add-opens=java.base/java.util.concurrent=ALL-UNNAMED", ex);
        }
    }
}
