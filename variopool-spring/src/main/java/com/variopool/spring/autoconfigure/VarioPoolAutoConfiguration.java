package com.variopool.spring.autoconfigure;

import com.variopool.core.model.VarioPoolConfig;
import com.variopool.core.refresh.ThreadPoolRefreshService;
import com.variopool.spring.listener.ConfigRefreshListener;
import com.variopool.spring.processor.VarioPoolBeanPostProcessor;
import com.variopool.spring.support.ApplicationContextHolder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnBean(VarioPoolMarkerConfiguration.Marker.class)
@ConditionalOnProperty(prefix = VarioPoolConfig.PREFIX, name = "enable", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(VarioPoolProperties.class)
public class VarioPoolAutoConfiguration {

    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    public ThreadPoolRefreshService threadPoolRefreshService() {
        return new ThreadPoolRefreshService();
    }

    @Bean
    public ConfigRefreshListener configRefreshListener(ThreadPoolRefreshService refreshService) {
        return new ConfigRefreshListener(refreshService);
    }

    @Bean
    public VarioPoolBeanPostProcessor varioPoolBeanPostProcessor(VarioPoolProperties properties) {
        return new VarioPoolBeanPostProcessor(properties);
    }
}
