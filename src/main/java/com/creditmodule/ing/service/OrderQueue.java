package com.creditmodule.ing.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class OrderQueue {
    private final BlockingQueue<Long> queue = new LinkedBlockingQueue<>();

    public void addOrder(Long orderId) {
        queue.offer(orderId);
    }

    public Long takeOrder() throws InterruptedException {
        return queue.take();
    }

}
