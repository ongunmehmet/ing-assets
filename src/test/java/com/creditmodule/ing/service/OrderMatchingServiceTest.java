package com.creditmodule.ing.service;


import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;
import com.creditmodule.ing.repository.AssetRepository;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMatchingServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private CustomerRepository customerRepository;

    private OrderQueue orderQueue;
    private OrderMatchingService orderMatchingService;

    @BeforeEach
    void setUp() {
        orderQueue = new OrderQueue(); // real instance
        orderMatchingService = new OrderMatchingService(
                orderRepository,
                assetRepository,
                customerRepository,
                orderQueue
        );
    }

    @Test
    void processOrder_shouldMatchBuyOrderSuccessfully() {
        var customer = TestUtils.customer("John", "Doe");
        customer.setCredit(BigDecimal.valueOf(5000));
        var asset = TestUtils.asset("Laptop", BigDecimal.TEN, BigDecimal.valueOf(1000));
        var order = TestUtils.order(customer, asset, Side.BUY, BigDecimal.valueOf(2), new Date());
        order.setStatus(Status.PENDING);
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(1L);

        assertEquals(Status.MATCHED, order.getStatus());
        assertEquals(BigDecimal.valueOf(3000), customer.getCredit());
        assertEquals(BigDecimal.valueOf(8), asset.getUsableSize());
        verify(orderRepository).save(order);
    }

    @Test
    void processOrder_shouldMatchSellOrderSuccessfully() {
        var customer = TestUtils.customer("John", "Doe");
        customer.setCredit(BigDecimal.valueOf(1000));
        var asset = TestUtils.asset("Laptop", BigDecimal.TEN, BigDecimal.valueOf(1000));
        var order = TestUtils.order(customer, asset, Side.SELL, BigDecimal.ONE, new Date());
        order.setStatus(Status.PENDING);
        order.setId(2L);

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(2L);

        assertEquals(Status.MATCHED, order.getStatus());
        assertEquals(BigDecimal.valueOf(2000), customer.getCredit());
        assertEquals(BigDecimal.valueOf(11), asset.getUsableSize());
        verify(orderRepository).save(order);
    }

    @Test
    void processOrder_shouldNotMatch_whenStatusIsNotPending() {
        var customer = TestUtils.customer("Dummy", "User");
        var asset = TestUtils.asset("Keyboard", BigDecimal.ONE, BigDecimal.valueOf(100));
        var order = TestUtils.order(customer, asset, Side.BUY, BigDecimal.ONE, new Date());
        order.setStatus(Status.MATCHED);
        order.setId(3L);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(3L);


        verify(orderRepository, never()).save(any());
    }

    @Test
    void processOrder_shouldRetry_onInsufficientCredit_whenTryCountLessThan5() {
        var customer = TestUtils.customer("Test", "User");
        customer.setCredit(BigDecimal.valueOf(100));
        var asset = TestUtils.asset("GPU", BigDecimal.valueOf(5), BigDecimal.valueOf(1000));
        var order = TestUtils.order(customer, asset, Side.BUY, BigDecimal.valueOf(2), new Date());
        order.setStatus(Status.PENDING);
        order.setId(4L);
        order.setTryCount(2);

        when(orderRepository.findById(4L))
                .thenReturn(Optional.of(order))
                .thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(4L);

        assertEquals(3, order.getTryCount());
        verify(orderRepository).save(order);
    }

    @Test
    void processOrder_shouldCancelOrder_whenTryCountReachesLimit() {
        var customer = TestUtils.customer("Fail", "User");
        var asset = TestUtils.asset("Memory", BigDecimal.valueOf(3), BigDecimal.valueOf(500));
        var order = TestUtils.order(customer, asset, Side.BUY, BigDecimal.ONE, new Date());
        order.setStatus(Status.PENDING);
        order.setId(5L);
        order.setTryCount(4);

        BigDecimal orderCost = asset.getInitialPrice().multiply(order.getSize());
        customer.setCredit(customer.getCredit().subtract(orderCost));

        when(orderRepository.findById(5L))
                .thenReturn(Optional.of(order))
                .thenReturn(Optional.of(order));
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(5L);

        assertEquals(Status.CANCELLED, order.getStatus());
        assertEquals(5, order.getTryCount());
        assertEquals(BigDecimal.valueOf(10000), customer.getCredit());
    }

    @Test
    void processOrder_shouldRefundBuyOrder_onFinalFailure() {
        var customer = TestUtils.customer("Refund", "User");
        customer.setCredit(BigDecimal.ZERO);
        var asset = TestUtils.asset("SSD", BigDecimal.ONE, BigDecimal.valueOf(250));
        var order = TestUtils.order(customer, asset, Side.BUY, BigDecimal.valueOf(2), new Date());
        order.setStatus(Status.PENDING);
        order.setId(6L);
        order.setTryCount(4);

        when(orderRepository.findById(6L))
                .thenReturn(Optional.of(order))
                .thenReturn(Optional.of(order));
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(6L);

        assertEquals(Status.CANCELLED, order.getStatus());
        assertEquals(5, order.getTryCount());
        assertEquals(BigDecimal.valueOf(500), customer.getCredit());
    }
}


