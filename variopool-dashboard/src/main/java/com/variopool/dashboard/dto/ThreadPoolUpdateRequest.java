package com.variopool.dashboard.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ThreadPoolUpdateRequest {

    @NotBlank
    private String appName;
    @NotBlank
    private String dataId;
    @NotBlank
    private String group;
    @NotBlank
    private String poolId;
    @NotNull
    @Min(1)
    @Max(512)
    private Integer corePoolSize;
    @NotNull
    @Min(1)
    @Max(512)
    private Integer maximumPoolSize;
    @NotNull
    @Min(0)
    @Max(9999999)
    private Integer queueCapacity;
    @NotBlank
    private String workQueue;
    @NotBlank
    private String rejectedHandler;
    @NotNull
    @Min(1)
    @Max(3600)
    private Long keepAliveSeconds;
    @NotNull
    private Boolean allowCoreThreadTimeout;
    private boolean syncRedis = true;
}
