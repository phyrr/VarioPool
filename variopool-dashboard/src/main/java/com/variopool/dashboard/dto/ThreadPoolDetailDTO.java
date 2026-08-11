package com.variopool.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThreadPoolDetailDTO {
    private String appName;
    private String dataId;
    private String group;
    private String poolId;
    private Integer corePoolSize;
    private Integer maximumPoolSize;
    private Integer queueCapacity;
    private String workQueue;
    private String rejectedHandler;
    private Long keepAliveSeconds;
    private Boolean allowCoreThreadTimeout;
    private Integer activeCount;
    private Integer poolSize;
    private Integer queueSize;
    private Long completedTaskCount;
    private Long rejectCount;
    private String agentUrl;
    private String configSource;
}
