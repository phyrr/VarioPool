package com.variopool.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thread pool configuration model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolConfig {

    private String poolId;
    private Integer corePoolSize;
    private Integer maximumPoolSize;
    private Integer queueCapacity;
    private String workQueue;
    private String rejectedHandler;
    private Long keepAliveSeconds;
    private Boolean allowCoreThreadTimeout;
}
