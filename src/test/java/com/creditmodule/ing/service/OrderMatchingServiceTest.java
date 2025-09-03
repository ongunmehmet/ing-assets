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

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMatchingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private CustomerRepository customerRepository;

    private OrderQueue orderQueue = new OrderQueue(); // real instance

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
        customer.setCredit(5000);
        var asset = TestUtils.asset("Laptop", 10.0, 1000.0);
        var order = TestUtils.order(customer, asset, Side.BUY, 2.0, new Date());
        order.setStatus(Status.PENDING);
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(1L);

        assertEquals(Status.MATCHED, order.getStatus());
        assertEquals(3000, customer.getCredit());
        assertEquals(8.0, asset.getUsableSize());
        verify(orderRepository).save(order);
    }

    @Test
    void processOrder_shouldMatchSellOrderSuccessfully() {
        var customer = TestUtils.customer("John", "Doe");
        customer.setCredit(1000);
        var asset = TestUtils.asset("Laptop", 10.0, 1000.0);
        var order = TestUtils.order(customer, asset, Side.SELL, 1.0, new Date());
        order.setStatus(Status.PENDING);
        order.setId(2L);

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(2L);

        assertEquals(Status.MATCHED, order.getStatus());
        assertEquals(2000, customer.getCredit());
        assertEquals(11.0, asset.getUsableSize());
        verify(orderRepository).save(order);
    }

    @Test
    void processOrder_shouldNotMatch_whenStatusIsNotPending() {
        var customer = TestUtils.customer("Dummy", "User");
        var asset = TestUtils.asset("Keyboard", 1.0, 100.0);
        var order = TestUtils.order(customer, asset, Side.BUY, 1.0, new Date());
        order.setStatus(Status.MATCHED); // not PENDING
        order.setId(3L);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        orderMatchingService.processOrder(3L);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void processOrder_shouldRetry_onInsufficientCredit_whenTryCountLessThan5() {
        var customer = TestUtils.customer("Test", "User");
        customer.setCredit(100);
        var asset = TestUtils.asset("GPU", 5.0, 1000.0);
        var order = TestUtils.order(customer, asset, Side.BUY, 2.0, new Date());
        order.setStatus(Status.PENDING);
        order.setId(4L);
        order.setTryCount(2);

        when(orderRepository.findById(4L))
                .thenReturn(Optional.of(order))
                .thenReturn(Optional.of(order)); // for retry block
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(4L);

        assertEquals(3, order.getTryCount());
        verify(orderRepository).save(order);
        // We skip asserting orderQueue due to real instance
    }

    @Test
    void processOrder_shouldCancelOrder_whenTryCountReachesLimit() {
        var customer = TestUtils.customer("Fail", "User");
        var asset = TestUtils.asset("Memory", 3.0, 500.0);
        var order = TestUtils.order(customer, asset, Side.BUY, 1.0, new Date());
        order.setStatus(Status.PENDING);
        order.setId(5L);
        order.setTryCount(4);

        when(orderRepository.findById(5L))
                .thenReturn(Optional.of(order))
                .thenReturn(Optional.of(order));
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(5L);

        assertEquals(Status.CANCELLED, order.getStatus());
        assertEquals(5, order.getTryCount());
        assertEquals(10000.0, customer.getCredit()); // refund worked
    }

    @Test
    void processOrder_shouldRefundBuyOrder_onFinalFailure() {
        var customer = TestUtils.customer("Refund", "User");
        customer.setCredit(0);
        var asset = TestUtils.asset("SSD", 1.0, 250.0);
        var order = TestUtils.order(customer, asset, Side.BUY, 2.0, new Date());
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
        assertEquals(500.0, customer.getCredit());
    }
}

