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
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            Asset asset = assetRepository.findByIdForUpdate(order.getAsset().getId());
            Customer customer = customerRepository.findByIdForUpdate(order.getCustomer().getId());

            BigDecimal price = asset.getInitialPrice();
            if (order.getStatus() != Status.PENDING) {
                return;
            }

            if (order.getOrderSide() == Side.BUY) {
                BigDecimal totalPrice = order.getSize().multiply(price);
                if (customer.getCredit().compareTo(totalPrice) < 0) {
                    throw new RuntimeException("Insufficient credit");
                }
                customer.setCredit(customer.getCredit().subtract(totalPrice));
                asset.setUsableSize(asset.getUsableSize().subtract(order.getSize()));
            } else {
                asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
                BigDecimal totalValue = order.getSize().multiply(price);
                customer.setCredit(customer.getCredit().add(totalValue));
            }

            order.setStatus(Status.MATCHED);
            orderRepository.save(order);
            log.info("Order {} matched successfully", orderId);

        } catch (Exception e) {
            log.error("Order {} failed: {}", orderId, e.getMessage());

            Order order = orderRepository.findById(orderId).orElseThrow();
            int tries = order.getTryCount() + 1;
            order.setTryCount(tries);

            if (tries >= 5) {
                // refund if it was a BUY order
                if (order.getOrderSide() == Side.BUY) {
                    Customer customer = customerRepository.findByIdForUpdate(order.getCustomer().getId());
                    BigDecimal refund = order.getSize().multiply(order.getAsset().getInitialPrice());
                    customer.setCredit(customer.getCredit().add(refund));
                }
                order.setStatus(Status.CANCELLED);
                log.warn("Order {} cancelled after 5 failures", orderId);
            } else {
                orderRepository.save(order);
                orderQueue.addOrder(orderId);
                log.info("Order {} requeued, try {}", orderId, tries);
            }
        }
    }

    @Transactional
    public void processSingleOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Status.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be matched");
        }

        Asset asset = assetRepository.findByIdForUpdate(order.getAsset().getId());
        Customer customer = customerRepository.findByIdForUpdate(order.getCustomer().getId());

        BigDecimal price = asset.getInitialPrice();

        if (order.getOrderSide() == Side.BUY) {
            BigDecimal totalPrice = order.getSize().multiply(price);
            if (customer.getCredit().compareTo(totalPrice) < 0) {
                throw new IllegalStateException("Insufficient credit");
            }
            customer.setCredit(customer.getCredit().subtract(totalPrice));
            asset.setUsableSize(asset.getUsableSize().subtract(order.getSize()));
        } else {
            asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            BigDecimal totalValue = order.getSize().multiply(price);
            customer.setCredit(customer.getCredit().add(totalValue));
        }

        order.setTryCount(order.getTryCount() + 1);

        if (order.getTryCount() >= 5) {
            order.setStatus(Status.CANCELLED);
            if (order.getOrderSide() == Side.BUY) {
                BigDecimal refund = order.getSize().multiply(asset.getInitialPrice());
                customer.setCredit(customer.getCredit().add(refund));
            }
        } else {
            order.setStatus(Status.MATCHED);
        }

        orderRepository.save(order);
    }
}
