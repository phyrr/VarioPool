package com.variopool.dashboard.service;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.variopool.dashboard.config.DashboardProperties;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class NacosConfigClient {

    private final ConfigService configService;

    public NacosConfigClient(DashboardProperties properties) throws NacosException {
        Properties nacosProperties = new Properties();
        nacosProperties.put("serverAddr", properties.getNacos().getServerAddr());
        this.configService = NacosFactory.createConfigService(nacosProperties);
    }

    public String getConfig(String dataId, String group) throws NacosException {
        return configService.getConfig(dataId, group, 5000);
    }

    public boolean publishConfig(String dataId, String group, String content) throws NacosException {
        return configService.publishConfig(dataId, group, content);
    }
}
