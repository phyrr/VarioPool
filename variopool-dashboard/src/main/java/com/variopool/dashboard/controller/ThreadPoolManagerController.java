package com.variopool.dashboard.controller;

import com.variopool.dashboard.common.Result;
import com.variopool.dashboard.config.DashboardProperties;
import com.variopool.dashboard.dto.RuntimeMetricsDTO;
import com.variopool.dashboard.dto.ThreadPoolDetailDTO;
import com.variopool.dashboard.dto.ThreadPoolUpdateRequest;
import com.variopool.dashboard.service.ThreadPoolManagerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ThreadPoolManagerController {

    private final ThreadPoolManagerService threadPoolManagerService;

    public ThreadPoolManagerController(ThreadPoolManagerService threadPoolManagerService) {
        this.threadPoolManagerService = threadPoolManagerService;
    }

    @GetMapping("/apps")
    public Result<List<DashboardProperties.AppConfig>> listApps() {
        return Result.success(threadPoolManagerService.listApps());
    }

    @GetMapping("/thread-pools")
    public Result<List<ThreadPoolDetailDTO>> listThreadPools(@RequestParam(required = false) String appName) {
        return Result.success(threadPoolManagerService.listThreadPools(appName));
    }

    @PutMapping("/thread-pool")
    public Result<Void> updateThreadPool(@RequestBody @Valid ThreadPoolUpdateRequest request) throws Exception {
        threadPoolManagerService.updateThreadPool(request);
        return Result.success();
    }

    @GetMapping("/thread-pools/metrics")
    public Result<List<RuntimeMetricsDTO>> listMetrics(@RequestParam(required = false) String appName,
                                                       @RequestParam(required = false) String poolId) {
        return Result.success(threadPoolManagerService.listRuntimeMetrics(appName, poolId));
    }
}
