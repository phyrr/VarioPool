package com.variopool.core.support;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public enum RejectedPolicyType {

    CALLER_RUNS_POLICY("CallerRunsPolicy", new ThreadPoolExecutor.CallerRunsPolicy()),
    ABORT_POLICY("AbortPolicy", new ThreadPoolExecutor.AbortPolicy()),
    DISCARD_POLICY("DiscardPolicy", new ThreadPoolExecutor.DiscardPolicy()),
    DISCARD_OLDEST_POLICY("DiscardOldestPolicy", new ThreadPoolExecutor.DiscardOldestPolicy());

    private final String name;
    private final RejectedExecutionHandler handler;
    private static final Map<String, RejectedPolicyType> LOOKUP = new HashMap<>();

    static {
        for (RejectedPolicyType type : values()) {
            LOOKUP.put(type.name, type);
        }
    }

    RejectedPolicyType(String name, RejectedExecutionHandler handler) {
        this.name = name;
        this.handler = handler;
    }

    public String getName() {
        return name;
    }

    public static RejectedExecutionHandler create(String policyName) {
        RejectedPolicyType type = LOOKUP.get(policyName);
        if (type == null) {
            throw new IllegalArgumentException("Unknown rejected policy: " + policyName);
        }
        return type.handler;
    }
}
