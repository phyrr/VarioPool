package com.variopool.dashboard.service;

import com.variopool.core.model.ThreadPoolConfig;
import com.variopool.core.model.VarioPoolConfig;
import com.variopool.core.parser.ConfigParser;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class YamlConfigWriter {

    private final ConfigParser configParser = new ConfigParser();
    private final Yaml yaml;

    public YamlConfigWriter() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        this.yaml = new Yaml(options);
    }

    public VarioPoolConfig parse(String content) {
        return configParser.parse(content);
    }

    public String updateExecutor(String content, ThreadPoolConfig updated) {
        Map<String, Object> root = loadRoot(content);
        Map<String, Object> variopool = getVariopoolNode(root);
        List<Map<String, Object>> executors = getExecutors(variopool);
        boolean found = false;
        for (Map<String, Object> executor : executors) {
            String poolId = asString(executor.get("pool-id"));
            if (Objects.equals(poolId, updated.getPoolId())) {
                applyExecutor(executor, updated);
                found = true;
                break;
            }
        }
        if (!found) {
            executors.add(toExecutorMap(updated));
        }
        variopool.put("executors", executors);
        root.put("variopool", variopool);
        return yaml.dump(root);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadRoot(String content) {
        if (content == null || content.isBlank()) {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("variopool", new LinkedHashMap<>());
            return root;
        }
        Object loaded = yaml.load(content);
        if (loaded instanceof Map<?, ?> map) {
            return new LinkedHashMap<>((Map<String, Object>) map);
        }
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("variopool", new LinkedHashMap<>());
        return root;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getVariopoolNode(Map<String, Object> root) {
        Object node = root.get("variopool");
        if (node instanceof Map<?, ?> map) {
            return new LinkedHashMap<>((Map<String, Object>) map);
        }
        Map<String, Object> variopool = new LinkedHashMap<>();
        root.put("variopool", variopool);
        return variopool;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getExecutors(Map<String, Object> variopool) {
        Object node = variopool.get("executors");
        if (node instanceof List<?> list) {
            List<Map<String, Object>> executors = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    executors.add(new LinkedHashMap<>((Map<String, Object>) map));
                }
            }
            return executors;
        }
        List<Map<String, Object>> executors = new ArrayList<>();
        variopool.put("executors", executors);
        return executors;
    }

    private void applyExecutor(Map<String, Object> executor, ThreadPoolConfig updated) {
        executor.put("pool-id", updated.getPoolId());
        executor.put("core-pool-size", updated.getCorePoolSize());
        executor.put("maximum-pool-size", updated.getMaximumPoolSize());
        executor.put("queue-capacity", updated.getQueueCapacity());
        executor.put("work-queue", updated.getWorkQueue());
        executor.put("rejected-handler", updated.getRejectedHandler());
        executor.put("keep-alive-time", updated.getKeepAliveSeconds());
        executor.put("allow-core-thread-time-out", updated.getAllowCoreThreadTimeout());
    }

    private Map<String, Object> toExecutorMap(ThreadPoolConfig config) {
        Map<String, Object> map = new LinkedHashMap<>();
        applyExecutor(map, config);
        return map;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
