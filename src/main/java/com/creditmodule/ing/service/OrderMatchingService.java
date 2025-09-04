package com.creditmodule.ing.service;

import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.Order;
import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;
import com.creditmodule.ing.repository.AssetRepository;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Slf4j
public class OrderMatchingService {
    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository;
    private final OrderQueue orderQueue;

    @Transactional
    public void processOrder(Long orderId) {

        process(orderId, true);
    }

    @Transactional
    public void processSingleOrder(Long orderId) {
        Order snapshot = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (snapshot.getStatus() != Status.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be matched");
        }

        process(orderId, false);
    }

    private void process(Long orderId, boolean requeueOnFail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Status.PENDING) return;

        Asset asset = assetRepository.findByIdForUpdate(order.getAsset().getId());
        Customer customer = customerRepository.findByIdForUpdate(order.getCustomer().getId());
        BigDecimal price = asset.getInitialPrice();

        boolean canMatch = canMatch(order, asset, customer, price);

        if (!canMatch) {
            handleFailedAttempt(order, asset, customer, price, requeueOnFail); // increments tryCount on failure
            return;
        }

        applyMatch(order, asset, customer, price);
        order.setStatus(Status.MATCHED);
        orderRepository.save(order);
        log.info("Order {} matched successfully", orderId);
    }


    private boolean canMatch(Order order, Asset asset, Customer customer, BigDecimal price) {
        if (order.getOrderSide() == Side.BUY) {

            return asset.getUsableSize().compareTo(order.getSize()) >= 0;
        } else {

            return true;
        }
    }

    private void applyMatch(Order order, Asset asset, Customer customer, BigDecimal price) {
        if (order.getOrderSide() == Side.BUY) {

            asset.setUsableSize(asset.getUsableSize().subtract(order.getSize()));
        } else {

            asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            BigDecimal totalValue = order.getSize().multiply(price);
            customer.setCredit(customer.getCredit().add(totalValue));
        }
    }


    private void handleFailedAttempt(
            Order order, Asset asset, Customer customer, BigDecimal price, boolean requeueOnFail) {

        int nextTry = order.getTryCount() + 1;
        order.setTryCount(nextTry);

        if (nextTry >= 5) {
            order.setStatus(Status.CANCELLED);

            if (order.getOrderSide() == Side.BUY) {
                BigDecimal refund = order.getSize().multiply(price);
                customer.setCredit(customer.getCredit().add(refund));
            }

            orderRepository.save(order);
            log.warn("Order {} cancelled after {} failures", order.getId(), nextTry);
            return;
        }


        orderRepository.save(order);
        if (requeueOnFail) {
            orderQueue.addOrder(order.getId());
            log.info("Order {} requeued, try {}", order.getId(), nextTry);
        } else {
            log.info("Order {} remains PENDING after manual attempt {}, not requeued", order.getId(), nextTry);
        }
    }
}
