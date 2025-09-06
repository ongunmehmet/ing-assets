package com.creditmodule.ing.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class OrderQueue {
    private final java.util.concurrent.BlockingQueue<Long> queue = new java.util.concurrent.LinkedBlockingQueue<>();
    private final java.util.Set<Long> enqueued = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public boolean addOrder(Long orderId) {
        if (enqueued.add(orderId)) {
            return queue.offer(orderId);
        }
        return false;
    }

    public Long takeOrder() throws InterruptedException {
        Long id = queue.take();
        enqueued.remove(id);
        return id;
    }
}
