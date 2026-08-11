package com.variopool.config.nacos;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.variopool.core.config.ConfigSource;
import com.variopool.core.refresh.ThreadPoolRefreshService;
import com.variopool.spring.autoconfigure.VarioPoolMarkerConfiguration;
import com.variopool.spring.autoconfigure.VarioPoolProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

@AutoConfiguration
@ConditionalOnBean(VarioPoolMarkerConfiguration.Marker.class)
@ConditionalOnClass(NacosConfigManager.class)
@ConditionalOnProperty(prefix = "variopool.nacos", name = "data-id")
public class NacosConfigAutoConfiguration {

    @Bean
    public NacosConfigSource nacosConfigSource(NacosConfigManager nacosConfigManager,
                                               VarioPoolProperties properties,
                                               ThreadPoolRefreshService refreshService) {
        return new NacosConfigSource(nacosConfigManager.getConfigService(), properties, refreshService);
    }

    @Bean
    public NacosConfigBootstrap nacosConfigBootstrap(NacosConfigSource nacosConfigSource) {
        return new NacosConfigBootstrap(nacosConfigSource);
    }

    public static class NacosConfigBootstrap {

        private final ConfigSource configSource;

        public NacosConfigBootstrap(ConfigSource configSource) {
            this.configSource = configSource;
        }

        @EventListener(ApplicationReadyEvent.class)
        public void onReady() {
            configSource.start();
        }
    }
}
