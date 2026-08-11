package com.variopool.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RuntimeMetricsDTO {
    private String poolId;
    private String agentUrl;
    private Integer corePoolSize;
    private Integer maximumPoolSize;
    private Integer poolSize;
    private Integer activeCount;
    private Integer queueSize;
    private Integer queueRemainingCapacity;
    private Long completedTaskCount;
    private Long rejectCount;
    private Double activeRate;
    private Double queueUsageRate;
}
