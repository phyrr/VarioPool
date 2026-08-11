package com.variopool.core.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Root configuration bound from Nacos YAML or Redis JSON/YAML payload.
 */
@Data
public class VarioPoolConfig {

    public static final String PREFIX = "variopool";

    private Boolean enable = Boolean.TRUE;
    private NacosConfig nacos;
    private RedisConfig redis;
    private List<ThreadPoolConfig> executors = new ArrayList<>();

    @Data
    public static class NacosConfig {
        private String dataId;
        private String group = "DEFAULT_GROUP";
    }

    @Data
    public static class RedisConfig {
        private String configKey;
        private String topic;
    }
}
