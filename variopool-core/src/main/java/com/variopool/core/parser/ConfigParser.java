package com.variopool.core.parser;

import com.variopool.core.model.ThreadPoolConfig;
import com.variopool.core.model.VarioPoolConfig;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Parses YAML configuration text into {@link VarioPoolConfig}.
 */
public class ConfigParser {

    private final Yaml yaml = new Yaml();

    public VarioPoolConfig parse(String content) {
        if (content == null || content.isBlank()) {
            return new VarioPoolConfig();
        }
        Object loaded = yaml.load(content);
        if (!(loaded instanceof Map<?, ?> root)) {
            return new VarioPoolConfig();
        }
        Object variopoolNode = root.get(VarioPoolConfig.PREFIX);
        if (!(variopoolNode instanceof Map<?, ?> configMap)) {
            return new VarioPoolConfig();
        }
        return mapConfig(configMap);
    }

    @SuppressWarnings("unchecked")
    private VarioPoolConfig mapConfig(Map<?, ?> configMap) {
        VarioPoolConfig config = new VarioPoolConfig();
        config.setEnable(asBoolean(configMap.get("enable"), Boolean.TRUE));

        Object nacosNode = configMap.get("nacos");
        if (nacosNode instanceof Map<?, ?> nacosMap) {
            VarioPoolConfig.NacosConfig nacos = new VarioPoolConfig.NacosConfig();
            nacos.setDataId(asString(nacosMap.get("data-id"), nacosMap.get("dataId")));
            nacos.setGroup(asString(nacosMap.get("group"), "DEFAULT_GROUP"));
            config.setNacos(nacos);
        }

        Object redisNode = configMap.get("redis");
        if (redisNode instanceof Map<?, ?> redisMap) {
            VarioPoolConfig.RedisConfig redis = new VarioPoolConfig.RedisConfig();
            redis.setConfigKey(asString(redisMap.get("config-key"), redisMap.get("configKey")));
            redis.setTopic(asString(redisMap.get("topic"), null));
            config.setRedis(redis);
        }

        Object executorsNode = configMap.get("executors");
        if (executorsNode instanceof List<?> list) {
            List<ThreadPoolConfig> executors = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> executorMap) {
                    executors.add(mapExecutor((Map<String, Object>) executorMap));
                }
            }
            config.setExecutors(executors);
        }
        return config;
    }

    private ThreadPoolConfig mapExecutor(Map<String, Object> map) {
        return ThreadPoolConfig.builder()
                .poolId(asString(map.get("pool-id"), map.get("poolId")))
                .corePoolSize(asInteger(map.get("core-pool-size"), map.get("corePoolSize")))
                .maximumPoolSize(asInteger(map.get("maximum-pool-size"), map.get("maximumPoolSize")))
                .queueCapacity(asInteger(map.get("queue-capacity"), map.get("queueCapacity")))
                .workQueue(asString(map.get("work-queue"), map.get("workQueue")))
                .rejectedHandler(asString(map.get("rejected-handler"), map.get("rejectedHandler")))
                .keepAliveSeconds(asLong(map.get("keep-alive-time"), map.get("keepAliveTime")))
                .allowCoreThreadTimeout(asBoolean(map.get("allow-core-thread-time-out"), map.get("allowCoreThreadTimeout")))
                .build();
    }

    private String asString(Object primary, Object fallback) {
        return Optional.ofNullable(primary).or(() -> Optional.ofNullable(fallback))
                .map(Object::toString).orElse(null);
    }

    private Integer asInteger(Object primary, Object fallback) {
        Object value = primary != null ? primary : fallback;
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    private Long asLong(Object primary, Object fallback) {
        Object value = primary != null ? primary : fallback;
        if (value == null) {
            return null;
        }
        return Long.parseLong(value.toString());
    }

    private Boolean asBoolean(Object primary, Object fallback) {
        Object value = primary != null ? primary : fallback;
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public Map<String, Object> flatten(String content) {
        VarioPoolConfig config = parse(content);
        Map<String, Object> flat = new LinkedHashMap<>();
        flat.put("config", config);
        return Collections.unmodifiableMap(flat);
    }
}
