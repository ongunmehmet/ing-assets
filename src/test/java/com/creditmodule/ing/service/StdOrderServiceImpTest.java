package com.creditmodule.ing.service;

import com.creditmodule.ing.entity.Order;
import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;
import com.creditmodule.ing.repository.AssetRepository;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StdOrderServiceImpTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks private StdOrderServiceImp orderService;

    @Test
    void createOrder_shouldSucceed_whenBuyOrderAndCreditSufficient() {
        var request = TestUtils.createOrderRequest(1L, "Laptop", Side.BUY, BigDecimal.valueOf(2));
        var customer = TestUtils.customer("John", "Doe");
        customer.setCredit(BigDecimal.valueOf(10_000));
        var asset = TestUtils.asset("Laptop", BigDecimal.valueOf(10), BigDecimal.valueOf(1000));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(assetRepository.findByAssetName("Laptop")).thenReturn(Optional.of(asset));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(10L);
            return o;
        });

        var response = orderService.createOrder(request);

        assertEquals(10L, response.getOrderId());
        assertEquals("Laptop", response.getAssetName());
        assertEquals(Status.PENDING, response.getStatus());
        assertEquals(Side.BUY, response.getOrderSide());
        assertEquals(BigDecimal.valueOf(2000), response.getTotalValue());
        verify(customerRepository).save(customer);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_shouldFail_whenCustomerNotFound() {
        var request = TestUtils.createOrderRequest(1L, "Laptop", Side.BUY, BigDecimal.valueOf(2));
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrder_shouldFail_whenAssetNotFound() {
        var request = TestUtils.createOrderRequest(1L, "Laptop", Side.BUY, BigDecimal.valueOf(2));
        var customer = TestUtils.customer("John", "Doe");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(assetRepository.findByAssetName("Laptop")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrder_shouldFail_whenCreditInsufficient() {
        var request = TestUtils.createOrderRequest(1L, "Laptop", Side.BUY, BigDecimal.valueOf(2));
        var customer = TestUtils.customer("John", "Doe");
        customer.setCredit(BigDecimal.valueOf(500)); // Not enough
        var asset = TestUtils.asset("Laptop", BigDecimal.valueOf(10), BigDecimal.valueOf(1000));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(assetRepository.findByAssetName("Laptop")).thenReturn(Optional.of(asset));

        assertThrows(IllegalStateException.class, () -> orderService.createOrder(request));
    }

    @Test
    void listOrders_shouldReturnOrdersWithinDateRange() {
        var customerId = 1L;
        var asset = TestUtils.asset("GPU", BigDecimal.valueOf(5), BigDecimal.valueOf(3000));
        var customer = TestUtils.customer("Alice", "Dev");

        var today = new Date();
        var order1 = TestUtils.order(customer, asset, Side.SELL, BigDecimal.ONE, today);
        var order2 = TestUtils.order(customer, asset, Side.BUY, BigDecimal.valueOf(2), today);

        when(orderRepository.findByCustomerIdAndCreateDateBetween(eq(customerId), any(), any()))
                .thenReturn(List.of(order1, order2));

        var response = orderService.listOrders(customerId, today, today);

        assertEquals(2, response.getOrders().size());
        assertEquals("GPU", response.getOrders().get(0).getAssetName());
    }

    @Test
    void deleteOrder_shouldSucceed_whenPendingBuyOrder() {
        var asset = TestUtils.asset("Monitor", BigDecimal.valueOf(2), BigDecimal.valueOf(500));
        var customer = TestUtils.customer("Test", "User");
        customer.setCredit(BigDecimal.valueOf(1000));

        var order = TestUtils.order(customer, asset, Side.BUY, BigDecimal.valueOf(2), new Date());
        order.setStatus(Status.PENDING);
        order.setId(15L);

        when(orderRepository.findById(15L)).thenReturn(Optional.of(order));

        var response = orderService.deleteOrder(15L);

        assertEquals("Monitor", response.getAssetName());
        assertEquals("Order cancelled successfully", response.getMessage());
        verify(orderRepository).delete(order);
        verify(customerRepository).save(customer);
    }

    @Test
    void deleteOrder_shouldFail_whenStatusNotPending() {
        var asset = TestUtils.asset("Monitor", BigDecimal.valueOf(2), BigDecimal.valueOf(500));
        var customer = TestUtils.customer("Test", "User");
        var order = TestUtils.order(customer, asset, Side.SELL, BigDecimal.valueOf(2), new Date());
        order.setStatus(Status.MATCHED);
        order.setId(20L);

        when(orderRepository.findById(20L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.deleteOrder(20L));
    }

    @Test
    void findOrder_shouldReturnOrder_whenExists() {
        var asset = TestUtils.asset("Keyboard", BigDecimal.ONE, BigDecimal.valueOf(100));
        var customer = TestUtils.customer("User", "Dev");
        var order = TestUtils.order(customer, asset, Side.SELL, BigDecimal.ONE, new Date());
        order.setId(99L);

        when(orderRepository.findById(99L)).thenReturn(Optional.of(order));

        var result = orderService.findOrder(99L);

        assertTrue(result.isPresent());
        assertEquals("Keyboard", result.get().getAsset().getAssetName());
    }

    @Test
    void findOrder_shouldReturnEmpty_whenNotFound() {
        when(orderRepository.findById(88L)).thenReturn(Optional.empty());

        var result = orderService.findOrder(88L);

        assertTrue(result.isEmpty());
    }
}
