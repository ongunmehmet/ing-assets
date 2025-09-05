package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateOrderRequest;
import com.creditmodule.ing.data.CreateOrderResponse;
import com.creditmodule.ing.data.DeleteOrderResponse;
import com.creditmodule.ing.data.OrderDetailDto;
import com.creditmodule.ing.data.OrderListDto;

import java.util.Date;

public interface IOrderService {
    CreateOrderResponse createOrder(CreateOrderRequest request);

    OrderListDto listOrders(Long customerId, Date startDate, Date endDate);

    DeleteOrderResponse deleteOrder(Long orderId);

    OrderDetailDto findOrder(Long id);
}

