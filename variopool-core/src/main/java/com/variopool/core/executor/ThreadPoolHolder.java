package com.variopool.core.executor;

import com.variopool.core.model.ThreadPoolConfig;
import lombok.Getter;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Holds executor instance and its last applied configuration.
 */
@Getter
public class ThreadPoolHolder {

    private final String poolId;
    private final ThreadPoolExecutor executor;
    private ThreadPoolConfig config;

    public ThreadPoolHolder(String poolId, ThreadPoolExecutor executor, ThreadPoolConfig config) {
        this.poolId = poolId;
        this.executor = executor;
        this.config = config;
    }

    public void updateConfig(ThreadPoolConfig config) {
        this.config = config;
    }
}
