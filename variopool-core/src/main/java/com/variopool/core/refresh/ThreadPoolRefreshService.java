package com.variopool.core.refresh;

import com.variopool.core.executor.ThreadPoolHolder;
import com.variopool.core.executor.VarioPoolRegistry;
import com.variopool.core.model.ThreadPoolConfig;
import com.variopool.core.model.VarioPoolConfig;
import com.variopool.core.parser.ConfigParser;
import com.variopool.core.queue.ResizableCapacityLinkedBlockingQueue;
import com.variopool.core.support.QueueType;
import com.variopool.core.support.RejectedPolicyType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Applies remote configuration to registered thread pools.
 */
@Slf4j
public class ThreadPoolRefreshService {

    private final ConfigParser configParser = new ConfigParser();

    public VarioPoolConfig parse(String content) {
        return configParser.parse(content);
    }

    public void refresh(String content) {
        VarioPoolConfig config = parse(content);
        if (Boolean.FALSE.equals(config.getEnable())) {
            log.info("VarioPool is disabled, skip refresh");
            return;
        }
        List<ThreadPoolConfig> executors = config.getExecutors();
        if (executors == null || executors.isEmpty()) {
            return;
        }
        for (ThreadPoolConfig remote : executors) {
            refreshOne(remote);
        }
    }

    public void refreshOne(ThreadPoolConfig remote) {
        if (remote == null || remote.getPoolId() == null) {
            return;
        }
        synchronized (remote.getPoolId().intern()) {
            ThreadPoolHolder holder = VarioPoolRegistry.get(remote.getPoolId());
            if (holder == null) {
                log.warn("Thread pool not found: {}", remote.getPoolId());
                return;
            }
            ThreadPoolConfig current = holder.getConfig();
            if (!hasChanges(current, remote, holder.getExecutor())) {
                return;
            }
            apply(holder.getExecutor(), current, remote);
            holder.updateConfig(copy(remote));
            log.info("[VarioPool] refreshed pool={}, core={} -> {}, max={} -> {}, queue={} -> {}",
                    remote.getPoolId(),
                    current.getCorePoolSize(), remote.getCorePoolSize(),
                    current.getMaximumPoolSize(), remote.getMaximumPoolSize(),
                    current.getQueueCapacity(), remote.getQueueCapacity());
        }
    }

    private void apply(ThreadPoolExecutor executor, ThreadPoolConfig before, ThreadPoolConfig after) {
        Integer core = after.getCorePoolSize();
        Integer max = after.getMaximumPoolSize();
        if (core != null && max != null) {
            if (core > executor.getMaximumPoolSize()) {
                executor.setMaximumPoolSize(max);
                executor.setCorePoolSize(core);
            } else {
                executor.setCorePoolSize(core);
                executor.setMaximumPoolSize(max);
            }
        } else {
            if (core != null) {
                executor.setCorePoolSize(core);
            }
            if (max != null) {
                executor.setMaximumPoolSize(max);
            }
        }

        if (after.getAllowCoreThreadTimeout() != null
                && !Objects.equals(before.getAllowCoreThreadTimeout(), after.getAllowCoreThreadTimeout())) {
            executor.allowCoreThreadTimeOut(after.getAllowCoreThreadTimeout());
        }

        if (after.getKeepAliveSeconds() != null
                && !Objects.equals(before.getKeepAliveSeconds(), after.getKeepAliveSeconds())) {
            executor.setKeepAliveTime(after.getKeepAliveSeconds(), TimeUnit.SECONDS);
        }

        if (after.getRejectedHandler() != null
                && !Objects.equals(before.getRejectedHandler(), after.getRejectedHandler())) {
            executor.setRejectedExecutionHandler(RejectedPolicyType.create(after.getRejectedHandler()));
        }

        if (isQueueCapacityChanged(before, after, executor)) {
            BlockingQueue<Runnable> queue = executor.getQueue();
            if (queue instanceof ResizableCapacityLinkedBlockingQueue<?> resizableQueue) {
                resizableQueue.setCapacity(after.getQueueCapacity());
            }
        }
    }

    private boolean hasChanges(ThreadPoolConfig before, ThreadPoolConfig after, ThreadPoolExecutor executor) {
        return changed(before.getCorePoolSize(), after.getCorePoolSize())
                || changed(before.getMaximumPoolSize(), after.getMaximumPoolSize())
                || changed(before.getKeepAliveSeconds(), after.getKeepAliveSeconds())
                || changed(before.getAllowCoreThreadTimeout(), after.getAllowCoreThreadTimeout())
                || changed(before.getRejectedHandler(), after.getRejectedHandler())
                || isQueueCapacityChanged(before, after, executor);
    }

    private <T> boolean changed(T before, T after) {
        return after != null && !Objects.equals(before, after);
    }

    private boolean isQueueCapacityChanged(ThreadPoolConfig before, ThreadPoolConfig after, ThreadPoolExecutor executor) {
        if (after.getQueueCapacity() == null || Objects.equals(before.getQueueCapacity(), after.getQueueCapacity())) {
            return false;
        }
        BlockingQueue<?> queue = executor.getQueue();
        return queue instanceof ResizableCapacityLinkedBlockingQueue<?>
                || QueueType.isResizable(before.getWorkQueue());
    }

    private ThreadPoolConfig copy(ThreadPoolConfig source) {
        return ThreadPoolConfig.builder()
                .poolId(source.getPoolId())
                .corePoolSize(source.getCorePoolSize())
                .maximumPoolSize(source.getMaximumPoolSize())
                .queueCapacity(source.getQueueCapacity())
                .workQueue(source.getWorkQueue())
                .rejectedHandler(source.getRejectedHandler())
                .keepAliveSeconds(source.getKeepAliveSeconds())
                .allowCoreThreadTimeout(source.getAllowCoreThreadTimeout())
                .build();
    }
}
