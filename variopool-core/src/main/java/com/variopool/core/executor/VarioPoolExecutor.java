package com.variopool.core.executor;

import com.variopool.core.model.ThreadPoolConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enhanced thread pool with pool id and reject counter.
 */
@Slf4j
@Getter
public class VarioPoolExecutor extends ThreadPoolExecutor {

    private final String poolId;
    private final AtomicLong rejectCount = new AtomicLong();

    public VarioPoolExecutor(@NonNull String poolId,
                             int corePoolSize,
                             int maximumPoolSize,
                             long keepAliveTime,
                             @NonNull TimeUnit unit,
                             @NonNull BlockingQueue<Runnable> workQueue,
                             @NonNull ThreadFactory threadFactory,
                             @NonNull RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.poolId = poolId;
        setRejectedExecutionHandler(handler);
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        RejectedExecutionHandler wrapper = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                rejectCount.incrementAndGet();
                handler.rejectedExecution(r, executor);
            }

            @Override
            public String toString() {
                return handler.getClass().getSimpleName();
            }
        };
        super.setRejectedExecutionHandler(wrapper);
    }

    public ThreadPoolConfig snapshotConfig() {
        return ThreadPoolConfig.builder()
                .poolId(poolId)
                .corePoolSize(getCorePoolSize())
                .maximumPoolSize(getMaximumPoolSize())
                .keepAliveSeconds(getKeepAliveTime(TimeUnit.SECONDS))
                .allowCoreThreadTimeout(allowsCoreThreadTimeOut())
                .rejectedHandler(getRejectedExecutionHandler().toString())
                .build();
    }
}
