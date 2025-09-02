package com.creditmodule.ing.service;

import com.creditmodule.ing.data.CreateOrderRequest;
import com.creditmodule.ing.data.CreateOrderResponse;
import com.creditmodule.ing.data.DeleteOrderResponse;
import com.creditmodule.ing.data.ListOrdersResponse;
import com.creditmodule.ing.entity.Asset;

import java.util.Date;
import java.util.List;

public interface IOrderService {

    CreateOrderResponse createOrder(CreateOrderRequest request);

    ListOrdersResponse listOrders(Long customerId, Date startDate, Date endDate);

    DeleteOrderResponse deleteOrder(Long orderId);

    List<Asset> listAssets(Long customerId);

    void matchPendingOrders(); // Admin endpoint
}

