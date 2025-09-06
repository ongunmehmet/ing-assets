package com.creditmodule.ing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderWorker {

    private final OrderQueue orderQueue;
    private final OrderMatchingService orderMatchingService;

    private volatile boolean running = false;

    private ExecutorService executor = Executors.newFixedThreadPool(5);


    public void start() {
        if (running) {
            log.info("Worker already running");
            return;
        }
        running = true;

        executor.submit(() -> {
            while (running) {
                try {
                    Long orderId = orderQueue.takeOrder();
                    executor.submit(() -> orderMatchingService.processOrder(orderId));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        log.info("OrderWorker started in parallel mode");
    }

    public void stop() {
        running = false;
        log.info("OrderWorker stopped");
    }

    public void processSingle(Long orderId) {

        executor.submit(() -> orderMatchingService.processOrder(orderId));
    }
}
