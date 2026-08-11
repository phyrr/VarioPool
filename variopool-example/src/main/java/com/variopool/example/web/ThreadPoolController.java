package com.variopool.example.web;

import com.variopool.core.executor.ThreadPoolHolder;
import com.variopool.core.executor.VarioPoolExecutor;
import com.variopool.core.executor.VarioPoolRegistry;
import com.variopool.config.redis.RedisConfigSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/variopool")
public class ThreadPoolController {

    @Autowired(required = false)
    private RedisConfigSource redisConfigSource;

    @GetMapping("/pools")
    public List<Map<String, Object>> listPools() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ThreadPoolHolder holder : VarioPoolRegistry.all()) {
            ThreadPoolExecutor executor = holder.getExecutor();
            BlockingQueue<Runnable> queue = executor.getQueue();
            Map<String, Object> item = new HashMap<>();
            item.put("poolId", holder.getPoolId());
            item.put("corePoolSize", executor.getCorePoolSize());
            item.put("maximumPoolSize", executor.getMaximumPoolSize());
            item.put("poolSize", executor.getPoolSize());
            item.put("activeCount", executor.getActiveCount());
            item.put("queueSize", queue.size());
            item.put("queueRemainingCapacity", queue.remainingCapacity());
            item.put("queueCapacity", holder.getConfig().getQueueCapacity());
            item.put("completedTaskCount", executor.getCompletedTaskCount());
            item.put("taskCount", executor.getTaskCount());
            item.put("largestPoolSize", executor.getLargestPoolSize());
            if (executor instanceof VarioPoolExecutor varioPoolExecutor) {
                item.put("rejectCount", varioPoolExecutor.getRejectCount().get());
            }
            item.put("workQueue", queue.getClass().getSimpleName());
            item.put("rejectedHandler", executor.getRejectedExecutionHandler().toString());
            item.put("config", holder.getConfig());
            result.add(item);
        }
        return result;
    }

    @PostMapping("/redis/refresh-signal")
    public Map<String, String> publishRedisRefreshSignal() {
        if (redisConfigSource == null) {
            return Map.of("status", "redis adapter not enabled");
        }
        redisConfigSource.publishRefreshSignal();
        return Map.of("status", "published");
    }
}
