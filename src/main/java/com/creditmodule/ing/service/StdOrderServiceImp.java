package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateOrderRequest;
import com.creditmodule.ing.data.CreateOrderResponse;
import com.creditmodule.ing.data.DeleteOrderResponse;
import com.creditmodule.ing.data.ListOrdersResponse;
import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.CustomerAsset;
import com.creditmodule.ing.entity.Order;
import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;
import com.creditmodule.ing.repository.AssetRepository;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class StdOrderServiceImp implements IOrderService {
    private CustomerRepository customerRepository;
    private AssetRepository assetRepository;
    private OrderRepository orderRepository;
    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Asset asset = assetRepository.findByAssetName(request.getAssetName())
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        double totalValue = request.getSize() * request.getPrice();

        if (request.getSide() == Side.BUY && customer.getCredit() < totalValue) {
            throw new IllegalStateException("Insufficient credit for this order");
        }

        // Deduct credit
        if (request.getSide() == Side.BUY) {
            customer.setCredit(customer.getCredit() - (long) totalValue);
            customerRepository.save(customer);
        }

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setAsset(asset);
        order.setOrderSide(request.getSide());
        order.setSize(request.getSize());
        order.setPrice(request.getPrice());
        order.setStatus(Status.PENDING);
        order.setCreateDate(new Date());

        orderRepository.save(order);

        return new CreateOrderResponse(
                order.getId(),
                asset.getAssetName(),
                order.getOrderSide(),
                order.getStatus(),
                order.getSize(),
                order.getPrice(),
                order.getCreateDate(),
                "Order created and pending"
        );
    }

    @Override
    public ListOrdersResponse listOrders(Long customerId, Date startDate, Date endDate) {
        List<Order> orders = orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);

        List<ListOrdersResponse.OrderDto> orderDtos = orders.stream().map(order ->
                new ListOrdersResponse.OrderDto(
                        order.getId(),
                        order.getAsset().getAssetName(),
                        order.getOrderSide(),
                        order.getStatus(),
                        order.getSize(),
                        order.getPrice(),
                        order.getCreateDate()
                )
        ).toList();

        return new ListOrdersResponse(orderDtos);
    }

    @Override
    public DeleteOrderResponse deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Status.PENDING) {
            throw new IllegalStateException("Only pending orders can be deleted");
        }

        Customer customer = order.getCustomer();
        if (order.getOrderSide() == Side.BUY) {
            double totalValue = order.getSize() * order.getPrice();
            customer.setCredit(customer.getCredit() + (long) totalValue); // Refund credit
            customerRepository.save(customer);
        }

        orderRepository.delete(order);

        return new DeleteOrderResponse(order.getId(), order.getAsset().getAssetName(), "Order cancelled successfully");
    }

    @Override
    public List<Asset> listAssets(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return customer.getCustomerAssets().stream()
                .map(CustomerAsset::getAsset)
                .toList();
    };
    @Override
    public void matchPendingOrders() {

    }

}

