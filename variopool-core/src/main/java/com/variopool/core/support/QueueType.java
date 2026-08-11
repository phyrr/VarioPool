package com.variopool.core.support;

import com.variopool.core.queue.ResizableCapacityLinkedBlockingQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public enum QueueType {

    ARRAY_BLOCKING_QUEUE("ArrayBlockingQueue") {
        @Override
        <T> BlockingQueue<T> create(Integer capacity) {
            return new ArrayBlockingQueue<>(capacity == null ? 4096 : capacity);
        }
    },
    LINKED_BLOCKING_QUEUE("LinkedBlockingQueue") {
        @Override
        <T> BlockingQueue<T> create(Integer capacity) {
            return capacity == null ? new LinkedBlockingQueue<>() : new LinkedBlockingQueue<>(capacity);
        }
    },
    RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE("ResizableCapacityLinkedBlockingQueue") {
        @Override
        <T> BlockingQueue<T> create(Integer capacity) {
            return capacity == null ? new ResizableCapacityLinkedBlockingQueue<>() : new ResizableCapacityLinkedBlockingQueue<>(capacity);
        }
    },
    SYNCHRONOUS_QUEUE("SynchronousQueue") {
        @Override
        <T> BlockingQueue<T> create(Integer capacity) {
            return new SynchronousQueue<>();
        }
    },
    LINKED_BLOCKING_DEQUE("LinkedBlockingDeque") {
        @Override
        <T> BlockingQueue<T> create(Integer capacity) {
            return capacity == null ? new LinkedBlockingDeque<>() : new LinkedBlockingDeque<>(capacity);
        }
    },
    LINKED_TRANSFER_QUEUE("LinkedTransferQueue") {
        @Override
        <T> BlockingQueue<T> create(Integer capacity) {
            return new LinkedTransferQueue<>();
        }
    },
    PRIORITY_BLOCKING_QUEUE("PriorityBlockingQueue") {
        @Override
        <T> BlockingQueue<T> create(Integer capacity) {
            return capacity == null ? new PriorityBlockingQueue<>() : new PriorityBlockingQueue<>(capacity);
        }
    };

    private final String name;
    private static final Map<String, QueueType> LOOKUP = new HashMap<>();

    static {
        for (QueueType type : values()) {
            LOOKUP.put(type.name, type);
        }
    }

    QueueType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    abstract <T> BlockingQueue<T> create(Integer capacity);

    public static <T> BlockingQueue<T> createQueue(String queueName, Integer capacity) {
        QueueType type = LOOKUP.get(queueName);
        if (type == null) {
            throw new IllegalArgumentException("Unknown queue type: " + queueName);
        }
        return type.create(capacity);
    }

    public static boolean isResizable(String queueName) {
        return Objects.equals(RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE.name, queueName);
    }
}
