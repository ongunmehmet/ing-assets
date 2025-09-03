package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateOrderRequest;
import com.creditmodule.ing.data.CreateOrderResponse;
import com.creditmodule.ing.data.DeleteOrderResponse;
import com.creditmodule.ing.data.ListOrdersResponse;
import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.entity.Order;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IOrderService {

    CreateOrderResponse createOrder(CreateOrderRequest request);

    ListOrdersResponse listOrders(Long customerId, Date startDate, Date endDate);

    DeleteOrderResponse deleteOrder(Long orderId);

    Optional<Order> findOrder(Long id);

}

