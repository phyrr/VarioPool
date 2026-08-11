package com.variopool.config.nacos;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.variopool.core.config.ConfigSource;
import com.variopool.core.model.VarioPoolConfig;
import com.variopool.core.refresh.ThreadPoolRefreshService;
import com.variopool.spring.autoconfigure.VarioPoolProperties;
import com.variopool.spring.event.ConfigRefreshedEvent;
import com.variopool.spring.support.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class NacosConfigSource implements ConfigSource {

    private final ConfigService configService;
    private final VarioPoolProperties properties;
    private final ThreadPoolRefreshService refreshService;

    public NacosConfigSource(ConfigService configService,
                             VarioPoolProperties properties,
                             ThreadPoolRefreshService refreshService) {
        this.configService = configService;
        this.properties = properties;
        this.refreshService = refreshService;
    }

    @Override
    public void start() {
        VarioPoolConfig.NacosConfig nacos = properties.getNacos();
        if (nacos == null || nacos.getDataId() == null) {
            log.warn("VarioPool nacos config is missing, skip nacos listener");
            return;
        }
        try {
            configService.addListener(nacos.getDataId(), nacos.getGroup(), new Listener() {
                @Override
                public Executor getExecutor() {
                    return Executors.newSingleThreadExecutor(r -> {
                        Thread thread = new Thread(r);
                        thread.setName("variopool-nacos-refresher");
                        thread.setDaemon(true);
                        return thread;
                    });
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    publish(configInfo);
                }
            });
            log.info("VarioPool nacos listener registered, dataId={}, group={}", nacos.getDataId(), nacos.getGroup());
            String initial = load();
            if (initial != null && !initial.isBlank()) {
                publish(initial);
            }
        } catch (NacosException ex) {
            throw new IllegalStateException("Failed to register nacos listener", ex);
        }
    }

    @Override
    public String load() {
        VarioPoolConfig.NacosConfig nacos = properties.getNacos();
        if (nacos == null || nacos.getDataId() == null) {
            return null;
        }
        try {
            return configService.getConfig(nacos.getDataId(), nacos.getGroup(), 5000);
        } catch (NacosException ex) {
            throw new IllegalStateException("Failed to load nacos config", ex);
        }
    }

    private void publish(String content) {
        VarioPoolConfig config = refreshService.parse(content);
        ApplicationContextHolder.publishEvent(new ConfigRefreshedEvent(this, content, config));
    }
}
