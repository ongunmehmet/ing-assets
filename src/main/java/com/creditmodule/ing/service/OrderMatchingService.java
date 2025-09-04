package com.creditmodule.ing.service;

import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.CustomerAsset;
import com.creditmodule.ing.entity.CustomerAssetId;
import com.creditmodule.ing.entity.Order;
import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;
import com.creditmodule.ing.repository.AssetRepository;
import com.creditmodule.ing.repository.CustomerAssetRepository;
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
    private final CustomerAssetRepository customerAssetRepository;   // ⬅ add this
    private final OrderQueue orderQueue;

    private static final BigDecimal ZERO = BigDecimal.ZERO;

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

        boolean canMatch = canMatch(order, asset, customer);

        if (!canMatch) {
            handleFailedAttempt(order, asset, customer, price, requeueOnFail);
            return;
        }

        applyMatch(order, asset, customer, price); // ⬅ will update CustomerAsset too
        order.setStatus(Status.MATCHED);
        orderRepository.save(order);
        log.info("Order {} matched successfully", orderId);
    }

    private boolean canMatch(Order order, Asset asset, Customer customer) {
        BigDecimal qty = nvl(order.getSize());
        if (order.getOrderSide() == Side.BUY) {
            // market must have enough available
            return nvl(asset.getUsableSize()).compareTo(qty) >= 0;
        } else {
            // SELL: verify customer has enough usable shares
            return customerAssetRepository.findByCustomerAndAsset(customer, asset)
                    .map(ca -> nvl(ca.getUsableSize()).compareTo(qty) >= 0)
                    .orElse(false);
        }
    }

    private void applyMatch(Order order, Asset asset, Customer customer, BigDecimal price) {
        BigDecimal qty = nvl(order.getSize());

        if (order.getOrderSide() == Side.BUY) {
            // 1) decrease market supply
            BigDecimal newUsable = nvl(asset.getUsableSize()).subtract(qty);
            if (newUsable.compareTo(ZERO) < 0) {
                throw new IllegalStateException("Not enough asset shares available");
            }
            asset.setUsableSize(newUsable);

            // 2) upsert CustomerAsset (increase size & usableSize)
            CustomerAsset ca = customerAssetRepository.findByCustomerAndAsset(customer, asset)
                    .orElseGet(() -> {
                        CustomerAssetId id = new CustomerAssetId();
                        id.setCustomerId(customer.getId());
                        id.setAssetId(asset.getId());
                        CustomerAsset x = new CustomerAsset();
                        x.setId(id);
                        x.setCustomer(customer);
                        x.setAsset(asset);
                        x.setSize(ZERO);
                        x.setUsableSize(ZERO);
                        return x;
                    });

            ca.setSize(nvl(ca.getSize()).add(qty));
            ca.setUsableSize(nvl(ca.getUsableSize()).add(qty));
            customerAssetRepository.save(ca);

            // NOTE: credit was already reserved at order creation for BUY; no change here.

        } else { // SELL
            // 1) fetch CustomerAsset (must exist and have enough usableSize)
            CustomerAsset ca = customerAssetRepository.findByCustomerAndAsset(customer, asset)
                    .orElseThrow(() -> new IllegalStateException("Customer does not own this asset"));

            BigDecimal newUsable = nvl(ca.getUsableSize()).subtract(qty);
            if (newUsable.compareTo(ZERO) < 0) {
                throw new IllegalStateException("Not enough usable shares to sell");
            }

            BigDecimal newSize = nvl(ca.getSize()).subtract(qty);

            // 2) update/delete holding
            if (newSize.compareTo(ZERO) == 0) {
                customerAssetRepository.delete(ca); // sold out -> remove row
            } else {
                ca.setSize(newSize);
                ca.setUsableSize(newUsable);
                customerAssetRepository.save(ca);
            }

            // 3) increase market supply
            asset.setUsableSize(nvl(asset.getUsableSize()).add(qty));

            // 4) credit customer proceeds (BUY reservation logic does not apply here)
            BigDecimal totalValue = qty.multiply(price);
            customer.setCredit(nvl(customer.getCredit()).add(totalValue));
        }
    }

    private void handleFailedAttempt(
            Order order, Asset asset, Customer customer, BigDecimal price, boolean requeueOnFail) {

        int nextTry = order.getTryCount() + 1;
        order.setTryCount(nextTry);

        if (nextTry >= 5) {
            order.setStatus(Status.CANCELLED);

            // BUY only blocks money (no asset blocked). Refund at final cancel:
            if (order.getOrderSide() == Side.BUY) {
                BigDecimal refund = nvl(order.getSize()).multiply(price);
                customer.setCredit(nvl(customer.getCredit()).add(refund));
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

    private static BigDecimal nvl(BigDecimal x) {
        return x == null ? ZERO : x;
    }
}
