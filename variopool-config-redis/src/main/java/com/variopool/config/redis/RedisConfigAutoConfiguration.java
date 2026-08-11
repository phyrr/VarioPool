package com.variopool.config.redis;

import com.variopool.core.config.ConfigSource;
import com.variopool.core.refresh.ThreadPoolRefreshService;
import com.variopool.spring.autoconfigure.VarioPoolMarkerConfiguration;
import com.variopool.spring.autoconfigure.VarioPoolProperties;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

@AutoConfiguration
@ConditionalOnBean(VarioPoolMarkerConfiguration.Marker.class)
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(prefix = "variopool.redis", name = "config-key")
public class RedisConfigAutoConfiguration {

    @Bean
    public RedisConfigSource redisConfigSource(RedissonClient redissonClient,
                                               VarioPoolProperties properties,
                                               ThreadPoolRefreshService refreshService) {
        return new RedisConfigSource(redissonClient, properties, refreshService);
    }

    @Bean
    public RedisConfigBootstrap redisConfigBootstrap(RedisConfigSource redisConfigSource) {
        return new RedisConfigBootstrap(redisConfigSource);
    }

    public static class RedisConfigBootstrap {

        private final ConfigSource configSource;

        public RedisConfigBootstrap(ConfigSource configSource) {
            this.configSource = configSource;
        }

        @EventListener(ApplicationReadyEvent.class)
        public void onReady() {
            configSource.start();
        }
    }
}
