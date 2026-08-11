package com.variopool.core.executor;

import com.variopool.core.model.ThreadPoolConfig;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Global registry for managed thread pools.
 */
public final class VarioPoolRegistry {

    private static final Map<String, ThreadPoolHolder> HOLDERS = new ConcurrentHashMap<>();

    private VarioPoolRegistry() {
    }

    public static void register(String poolId, ThreadPoolExecutor executor, ThreadPoolConfig config) {
        HOLDERS.put(poolId, new ThreadPoolHolder(poolId, executor, config));
    }

    public static ThreadPoolHolder get(String poolId) {
        return HOLDERS.get(poolId);
    }

    public static Collection<ThreadPoolHolder> all() {
        return HOLDERS.values();
    }
}
