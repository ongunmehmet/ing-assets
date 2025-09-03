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

            double price = asset.getInitialPrice();
            if (order.getStatus() != Status.PENDING) {
                return;
            }

            if (order.getOrderSide() == Side.BUY) {
                double totalPrice = order.getSize() * price;
                if (customer.getCredit() < totalPrice) {
                    throw new RuntimeException("Insufficient credit");
                }
                customer.setCredit(customer.getCredit() - totalPrice);
                asset.setUsableSize(asset.getUsableSize() - order.getSize());
            } else {

                asset.setUsableSize(asset.getUsableSize() + order.getSize());
                customer.setCredit(customer.getCredit() + (order.getSize() * price));
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
                order.setStatus(Status.CANCELLED);
                log.warn("Order {} cancelled after 5 failures", orderId);
            } else {
                orderRepository.save(order);
                orderQueue.addOrder(orderId); // requeue
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

        double price = asset.getInitialPrice();
        if (order.getOrderSide() == Side.BUY) {
            double totalPrice = order.getSize() * price;
            if (customer.getCredit() < totalPrice) {
                throw new IllegalStateException("Insufficient credit");
            }
            customer.setCredit(customer.getCredit() - totalPrice);
            asset.setUsableSize(asset.getUsableSize() - order.getSize());
        } else {

            asset.setUsableSize(asset.getUsableSize() + order.getSize());
            customer.setCredit(customer.getCredit() + (order.getSize() * price));
        }

        order.setStatus(Status.MATCHED);
        order.setTryCount(order.getTryCount() + 1);
        if (order.getTryCount() >= 5) {
            order.setStatus(Status.CANCELLED);
        }
        orderRepository.save(order);
    }
}

