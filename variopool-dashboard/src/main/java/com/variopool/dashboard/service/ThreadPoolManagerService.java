package com.variopool.dashboard.service;

import com.variopool.core.model.ThreadPoolConfig;
import com.variopool.core.model.VarioPoolConfig;
import com.variopool.dashboard.config.DashboardProperties;
import com.variopool.dashboard.dto.RuntimeMetricsDTO;
import com.variopool.dashboard.dto.ThreadPoolDetailDTO;
import com.variopool.dashboard.dto.ThreadPoolUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadPoolManagerService {

    private final DashboardProperties properties;
    private final NacosConfigClient nacosConfigClient;
    private final YamlConfigWriter yamlConfigWriter;
    private final RedissonClient redissonClient;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<ThreadPoolDetailDTO> listThreadPools(String appName) {
        List<ThreadPoolDetailDTO> result = new ArrayList<>();
        for (DashboardProperties.AppConfig app : properties.getApps()) {
            if (appName != null && !appName.isBlank() && !Objects.equals(app.getName(), appName)) {
                continue;
            }
            try {
                String content = nacosConfigClient.getConfig(app.getDataId(), app.getGroup());
                VarioPoolConfig config = yamlConfigWriter.parse(content);
                Map<String, Map<String, Object>> runtimeMap = fetchRuntimeMap(app.getAgentUrl());
                for (ThreadPoolConfig poolConfig : config.getExecutors()) {
                    Map<String, Object> runtime = runtimeMap.get(poolConfig.getPoolId());
                    result.add(toDetail(app, poolConfig, runtime, "Nacos"));
                }
            } catch (Exception ex) {
                log.error("Failed to load thread pools for app {}", app.getName(), ex);
            }
        }
        return result;
    }

    public void updateThreadPool(ThreadPoolUpdateRequest request) throws Exception {
        DashboardProperties.AppConfig app = findApp(request.getAppName());
        String content = nacosConfigClient.getConfig(app.getDataId(), app.getGroup());
        ThreadPoolConfig updated = ThreadPoolConfig.builder()
                .poolId(request.getPoolId())
                .corePoolSize(request.getCorePoolSize())
                .maximumPoolSize(request.getMaximumPoolSize())
                .queueCapacity(request.getQueueCapacity())
                .workQueue(request.getWorkQueue())
                .rejectedHandler(request.getRejectedHandler())
                .keepAliveSeconds(request.getKeepAliveSeconds())
                .allowCoreThreadTimeout(request.getAllowCoreThreadTimeout())
                .build();
        String newContent = yamlConfigWriter.updateExecutor(content, updated);
        nacosConfigClient.publishConfig(app.getDataId(), app.getGroup(), newContent);
        if (request.isSyncRedis()) {
            syncRedis(newContent);
        }
    }

    public List<RuntimeMetricsDTO> listRuntimeMetrics(String appName, String poolId) {
        List<RuntimeMetricsDTO> metrics = new ArrayList<>();
        for (DashboardProperties.AppConfig app : properties.getApps()) {
            if (appName != null && !appName.isBlank() && !Objects.equals(app.getName(), appName)) {
                continue;
            }
            try {
                List<Map<String, Object>> pools = restTemplate.getForObject(app.getAgentUrl() + "/variopool/pools", List.class);
                if (pools == null) {
                    continue;
                }
                for (Map<String, Object> pool : pools) {
                    String currentPoolId = asString(pool.get("poolId"));
                    if (poolId != null && !poolId.isBlank() && !Objects.equals(currentPoolId, poolId)) {
                        continue;
                    }
                    metrics.add(toMetrics(app.getAgentUrl(), pool));
                }
            } catch (Exception ex) {
                log.warn("Failed to fetch runtime metrics from {}", app.getAgentUrl(), ex);
            }
        }
        return metrics;
    }

    public List<DashboardProperties.AppConfig> listApps() {
        return properties.getApps();
    }

    private void syncRedis(String content) {
        DashboardProperties.Redis redis = properties.getRedis();
        RBucket<String> bucket = redissonClient.getBucket(redis.getConfigKey());
        bucket.set(content);
        RTopic topic = redissonClient.getTopic(redis.getTopic());
        topic.publish(Map.of("action", "REFRESH"));
    }

    private DashboardProperties.AppConfig findApp(String appName) {
        return properties.getApps().stream()
                .filter(app -> Objects.equals(app.getName(), appName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown app: " + appName));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> fetchRuntimeMap(String agentUrl) {
        try {
            List<Map<String, Object>> pools = restTemplate.getForObject(agentUrl + "/variopool/pools", List.class);
            if (pools == null) {
                return Map.of();
            }
            return pools.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            item -> asString(item.get("poolId")),
                            item -> item,
                            (a, b) -> a
                    ));
        } catch (Exception ex) {
            log.warn("Runtime unavailable from {}", agentUrl);
            return Map.of();
        }
    }

    private ThreadPoolDetailDTO toDetail(DashboardProperties.AppConfig app,
                                         ThreadPoolConfig config,
                                         Map<String, Object> runtime,
                                         String configSource) {
        return ThreadPoolDetailDTO.builder()
                .appName(app.getName())
                .dataId(app.getDataId())
                .group(app.getGroup())
                .poolId(config.getPoolId())
                .corePoolSize(config.getCorePoolSize())
                .maximumPoolSize(config.getMaximumPoolSize())
                .queueCapacity(config.getQueueCapacity())
                .workQueue(config.getWorkQueue())
                .rejectedHandler(config.getRejectedHandler())
                .keepAliveSeconds(config.getKeepAliveSeconds())
                .allowCoreThreadTimeout(config.getAllowCoreThreadTimeout())
                .activeCount(asInteger(runtime == null ? null : runtime.get("activeCount")))
                .poolSize(asInteger(runtime == null ? null : runtime.get("poolSize")))
                .queueSize(asInteger(runtime == null ? null : runtime.get("queueSize")))
                .completedTaskCount(asLong(runtime == null ? null : runtime.get("completedTaskCount")))
                .rejectCount(asLong(runtime == null ? null : runtime.get("rejectCount")))
                .agentUrl(app.getAgentUrl())
                .configSource(configSource)
                .build();
    }

    private RuntimeMetricsDTO toMetrics(String agentUrl, Map<String, Object> pool) {
        int activeCount = asInteger(pool.get("activeCount"));
        int maximumPoolSize = asInteger(pool.get("maximumPoolSize"));
        int queueSize = asInteger(pool.get("queueSize"));
        int queueCapacity = asInteger(pool.get("queueCapacity"));
        double activeRate = maximumPoolSize == 0 ? 0 : activeCount * 100.0 / maximumPoolSize;
        double queueUsageRate = queueCapacity == 0 ? 0 : queueSize * 100.0 / queueCapacity;
        return RuntimeMetricsDTO.builder()
                .poolId(asString(pool.get("poolId")))
                .agentUrl(agentUrl)
                .corePoolSize(asInteger(pool.get("corePoolSize")))
                .maximumPoolSize(maximumPoolSize)
                .poolSize(asInteger(pool.get("poolSize")))
                .activeCount(activeCount)
                .queueSize(queueSize)
                .queueRemainingCapacity(asInteger(pool.get("queueRemainingCapacity")))
                .completedTaskCount(asLong(pool.get("completedTaskCount")))
                .rejectCount(asLong(pool.get("rejectCount")))
                .activeRate(activeRate)
                .queueUsageRate(queueUsageRate)
                .build();
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private int asInteger(Object value) {
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

    private long asLong(Object value) {
        return value == null ? 0L : Long.parseLong(value.toString());
    }
}
