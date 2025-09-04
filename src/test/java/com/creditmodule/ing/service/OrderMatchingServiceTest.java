package com.creditmodule.ing.service;


import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.Order;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        orderQueue = new OrderQueue(); // real queue (we won't assert requeue directly)
        orderMatchingService = new OrderMatchingService(
                orderRepository,
                assetRepository,
                customerRepository,
                orderQueue
        );
    }

    @Test
    void processOrder_shouldMatchBuyOrderSuccessfully() {
        Customer customer = TestUtils.customer("John", "Doe");
        customer.setCredit(new BigDecimal("5000")); // blocked at create time (unchanged on match)

        Asset asset = TestUtils.asset("Laptop", new BigDecimal("10"), new BigDecimal("1000"));
        Order order = TestUtils.order(customer, asset, Side.BUY, new BigDecimal("2"), new Date());
        order.setStatus(Status.PENDING);
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(1L);

        assertEquals(Status.MATCHED, order.getStatus());
        // BUY success does NOT change credit at match-time
        assertEquals(new BigDecimal("5000"), customer.getCredit());
        // asset usable size went from 10 to 8
        assertEquals(new BigDecimal("8"), asset.getUsableSize());
        // no tryCount increment on success
        assertEquals(0, order.getTryCount());
        verify(orderRepository).save(order);
    }

    @Test
    void processOrder_shouldMatchSellOrderSuccessfully() {
        Customer customer = TestUtils.customer("John", "Doe");
        customer.setCredit(new BigDecimal("1000")); // will increase by price*size

        Asset asset = TestUtils.asset("Laptop", new BigDecimal("10"), new BigDecimal("1000"));
        Order order = TestUtils.order(customer, asset, Side.SELL, BigDecimal.ONE, new Date());
        order.setStatus(Status.PENDING);
        order.setId(2L);

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(2L);

        assertEquals(Status.MATCHED, order.getStatus());
        // SELL success credits customer: 1000 + (1 * 1000) = 2000
        assertEquals(new BigDecimal("2000"), customer.getCredit());
        // asset usable size increased: 10 -> 11
        assertEquals(new BigDecimal("11"), asset.getUsableSize());
        // no tryCount increment on success
        assertEquals(0, order.getTryCount());
        verify(orderRepository).save(order);
    }

    @Test
    void processOrder_shouldDoNothing_whenStatusIsNotPending() {
        Customer customer = TestUtils.customer("Dummy", "User");
        Asset asset = TestUtils.asset("Keyboard", BigDecimal.ONE, new BigDecimal("100"));
        Order order = TestUtils.order(customer, asset, Side.BUY, BigDecimal.ONE, new Date());
        order.setStatus(Status.MATCHED);
        order.setId(3L);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        orderMatchingService.processOrder(3L);

        verify(orderRepository, never()).save(any());
        // ensure no changes
        assertEquals(Status.MATCHED, order.getStatus());
    }

    @Test
    void processOrder_shouldRetry_onInsufficientSupply_whenTryCountLessThan5() {
        Customer customer = TestUtils.customer("Test", "User");
        customer.setCredit(new BigDecimal("10000"));

        // insufficient supply for BUY: usableSize(1) < order size(2)
        Asset asset = TestUtils.asset("GPU", new BigDecimal("1"), new BigDecimal("1000"));
        Order order = TestUtils.order(customer, asset, Side.BUY, new BigDecimal("2"), new Date());
        order.setStatus(Status.PENDING);
        order.setId(4L);
        order.setTryCount(2);

        when(orderRepository.findById(4L))
                .thenReturn(Optional.of(order))
                .thenReturn(Optional.of(order)); // service re-reads in error path
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(4L);

        // failure increments tryCount by 1
        assertEquals(3, order.getTryCount());
        // still pending
        assertEquals(Status.PENDING, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void processOrder_shouldCancelOrder_whenTryCountReachesLimit() {
        Customer customer = TestUtils.customer("Fail", "User");
        Asset asset = TestUtils.asset("Memory", new BigDecimal("0"), new BigDecimal("500")); // zero supply → always fail
        Order order = TestUtils.order(customer, asset, Side.BUY, BigDecimal.ONE, new Date());
        order.setStatus(Status.PENDING);
        order.setId(5L);
        order.setTryCount(4); // next failure cancels

        // Simulate blocked credit at create-time: subtract upfront so refund returns you to original
        BigDecimal orderCost = asset.getInitialPrice().multiply(order.getSize()); // 500 * 1
        customer.setCredit(customer.getCredit().subtract(orderCost));

        when(orderRepository.findById(5L))
                .thenReturn(Optional.of(order))
                .thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(5L);

        assertEquals(Status.CANCELLED, order.getStatus());
        assertEquals(5, order.getTryCount());
        // refunded to original credit (TestUtils default + refund - earlier block)
        // if your TestUtils starts at 10000, it returns to 10000
        assertEquals(new BigDecimal("10000"), customer.getCredit());
        verify(orderRepository).save(order);
    }

    @Test
    void processOrder_shouldRefundBuyOrder_onFinalFailure_withoutPriorManualBlockSetup() {
        // This test demonstrates refund even if you did not pre-subtract in the test
        Customer customer = TestUtils.customer("Refund", "User");
        customer.setCredit(new BigDecimal("0")); // start 0 to make refund observable
        Asset asset = TestUtils.asset("SSD", BigDecimal.ZERO, new BigDecimal("250")); // no supply → fail
        Order order = TestUtils.order(customer, asset, Side.BUY, new BigDecimal("2"), new Date()); // refund = 500
        order.setStatus(Status.PENDING);
        order.setId(6L);
        order.setTryCount(4);

        when(orderRepository.findById(6L))
                .thenReturn(Optional.of(order))
                .thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(6L);

        assertEquals(Status.CANCELLED, order.getStatus());
        assertEquals(5, order.getTryCount());
        assertEquals(new BigDecimal("500"), customer.getCredit()); // refund added
        verify(orderRepository).save(order);
    }

    @Test
    void processSingleOrder_shouldMatchBuyOrderSuccessfully() {
        Customer customer = TestUtils.customer("John", "Manual");
        customer.setCredit(new BigDecimal("5000"));
        Asset asset = TestUtils.asset("GPU", new BigDecimal("10"), new BigDecimal("1000"));
        Order order = TestUtils.order(customer, asset, Side.BUY, new BigDecimal("2"), new Date());
        order.setId(1L);
        order.setStatus(Status.PENDING);
        order.setTryCount(0);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);

        orderMatchingService.processSingleOrder(1L);

        assertEquals(Status.MATCHED, order.getStatus());
        // BUY success does not change credit
        assertEquals(new BigDecimal("5000"), customer.getCredit());
        assertEquals(new BigDecimal("8"), asset.getUsableSize());
        // no increment on success
        assertEquals(0, order.getTryCount());
        verify(orderRepository).save(order);
    }

    @Test
    void processSingleOrder_shouldFail_whenOrderNotFound() {
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderMatchingService.processSingleOrder(10L));
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void processSingleOrder_shouldFail_whenOrderIsNotPending() {
        Order order = TestUtils.order(null, null, Side.BUY, BigDecimal.ONE, new Date());
        order.setId(2L);
        order.setStatus(Status.MATCHED);

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderMatchingService.processSingleOrder(2L));
    }

    @Test
    void processSingleOrder_shouldIncrementTryCount_onInsufficientSupply_andRemainPending() {
        Customer customer = TestUtils.customer("Low", "Supply");
        customer.setCredit(new BigDecimal("5000"));
        Asset asset = TestUtils.asset("SSD", new BigDecimal("1"), new BigDecimal("1000"));
        Order order = TestUtils.order(customer, asset, Side.BUY, new BigDecimal("2"), new Date()); // needs 2, have 1
        order.setStatus(Status.PENDING);
        order.setTryCount(0);
        order.setId(3L);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processSingleOrder(3L);

        assertEquals(1, order.getTryCount());
        assertEquals(Status.PENDING, order.getStatus()); // manual does not requeue, stays pending
        verify(orderRepository).save(order);
    }

    @Test
    void processSingleOrder_shouldCancelOrder_whenTryCountLimitReached() {
        Customer customer = TestUtils.customer("Retry", "Limit");
        customer.setCredit(BigDecimal.ZERO);
        Asset asset = TestUtils.asset("RAM", new BigDecimal("1"), new BigDecimal("250"));
        Order order = TestUtils.order(customer, asset, Side.BUY, new BigDecimal("2"), new Date());
        order.setStatus(Status.PENDING);
        order.setTryCount(4); // next failure hits limit
        order.setId(4L);

        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processSingleOrder(4L);

        assertEquals(Status.CANCELLED, order.getStatus());
        assertEquals(5, order.getTryCount());
        // refund price*size = 250*2 = 500
        assertEquals(new BigDecimal("500"), customer.getCredit()); // if initial credit was 0
        verify(orderRepository).save(order);
    }

    @Test
    void processSingleOrder_shouldMatchSellOrderSuccessfully() {
        Customer customer = TestUtils.customer("Sell", "User");
        customer.setCredit(BigDecimal.ZERO);
        Asset asset = TestUtils.asset("Tablet", new BigDecimal("5"), new BigDecimal("1000"));
        Order order = TestUtils.order(customer, asset, Side.SELL, new BigDecimal("1"), new Date());
        order.setStatus(Status.PENDING);
        order.setTryCount(0);
        order.setId(5L);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processSingleOrder(5L);

        assertEquals(Status.MATCHED, order.getStatus());
        assertEquals(new BigDecimal("1000"), customer.getCredit());
        assertEquals(new BigDecimal("6"), asset.getUsableSize());
        // no increment on success
        assertEquals(0, order.getTryCount());
        verify(orderRepository).save(order);
    }
}



