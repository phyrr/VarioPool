package com.variopool.example.config;

import com.variopool.core.executor.VarioPoolExecutor;
import com.variopool.core.queue.ResizableCapacityLinkedBlockingQueue;
import com.variopool.spring.annotation.VarioPoolBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class DemoThreadPoolConfiguration {

    private final AtomicInteger threadIndex = new AtomicInteger(1);

    @Bean
    @VarioPoolBean
    public VarioPoolExecutor orderExecutor() {
        return new VarioPoolExecutor(
                "order-pool",
                4,
                8,
                60,
                TimeUnit.SECONDS,
                new ResizableCapacityLinkedBlockingQueue<>(200),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("order-pool-" + threadIndex.getAndIncrement());
                    thread.setDaemon(false);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
