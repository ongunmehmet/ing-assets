package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateOrderRequest;
import com.creditmodule.ing.data.CreateOrderResponse;
import com.creditmodule.ing.data.DeleteOrderResponse;
import com.creditmodule.ing.data.OrderAssetLineDto;
import com.creditmodule.ing.data.OrderCustomerLineDto;
import com.creditmodule.ing.data.OrderDetailDto;
import com.creditmodule.ing.data.OrderListDto;
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
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class StdOrderServiceImp implements IOrderService {

    private final CustomerRepository customerRepository;
    private final AssetRepository assetRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Asset asset = assetRepository.findByAssetName(request.getAssetName())
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        BigDecimal price = asset.getInitialPrice();
        BigDecimal totalValue = request.getSize().multiply(price);

        if (request.getSide() == Side.BUY && customer.getCredit().compareTo(totalValue) < 0) {
            throw new IllegalStateException("Insufficient credit for this order");
        }

        if (request.getSide() == Side.BUY) {
            customer.setCredit(customer.getCredit().subtract(totalValue));
            customerRepository.save(customer);
        }

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setAsset(asset);
        order.setOrderSide(request.getSide());
        order.setSize(request.getSize());
        order.setStatus(Status.PENDING);
        order.setCreateDate(new Date());

        orderRepository.save(order);

        return new CreateOrderResponse(
                order.getId(),
                asset.getAssetName(),
                order.getOrderSide(),
                order.getStatus(),
                order.getSize(),
                totalValue,
                order.getCreateDate(),
                "Order created and pending"
        );
    }

    @Override
    public OrderListDto listOrders(Long customerId, Date startDate, Date endDate) {
        List<Order> orders =
                orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);

        List<OrderDetailDto> details = orders.stream()
                .map(order -> new OrderDetailDto(
                        order.getId(),
                        order.getOrderSide(),
                        order.getStatus(),
                        order.getSize(),
                        order.getCreateDate(),
                        order.getTryCount(),
                        new OrderCustomerLineDto(
                                order.getCustomer().getId(),
                                order.getCustomer().getName(),
                                order.getCustomer().getSurname()
                        ),
                        new OrderAssetLineDto(
                                order.getAsset().getId(),
                                order.getAsset().getAssetName()
                        )
                ))
                .toList();

        return new OrderListDto(details);
    }

    @Override
    @Transactional
    public DeleteOrderResponse deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Status.PENDING) {
            throw new IllegalStateException("Only pending orders can be deleted");
        }

        Customer customer = order.getCustomer();
        if (order.getOrderSide() == Side.BUY) {
            BigDecimal totalValue = order.getSize().multiply(order.getAsset().getInitialPrice());
            customer.setCredit(customer.getCredit().add(totalValue)); // Refund
            customerRepository.save(customer);
        }

        orderRepository.delete(order);

        return new DeleteOrderResponse(
                order.getId(),
                order.getAsset().getAssetName(),
                "Order cancelled successfully"
        );
    }

    @Override
    public OrderDetailDto findOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return new OrderDetailDto(
                order.getId(),
                order.getOrderSide(),
                order.getStatus(),
                order.getSize(),
                order.getCreateDate(),
                order.getTryCount(),
                new OrderCustomerLineDto(
                        order.getCustomer().getId(),
                        order.getCustomer().getName(),
                        order.getCustomer().getSurname()
                ),
                new OrderAssetLineDto(
                        order.getAsset().getId(),
                        order.getAsset().getAssetName()
                )
        );
    }
}


