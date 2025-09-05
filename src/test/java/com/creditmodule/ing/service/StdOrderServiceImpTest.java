package com.creditmodule.ing.service;

import com.creditmodule.ing.data.OrderDetailDto;
import com.creditmodule.ing.data.OrderListDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    void listOrders_shouldReturnOrderDetailDtosWithinDateRange() {
        var customerId = 1L;
        var asset = TestUtils.asset("GPU", BigDecimal.valueOf(5), BigDecimal.valueOf(3000));
        var customer = TestUtils.customer("Alice", "Dev");

        var today = new Date();
        var order1 = TestUtils.order(customer, asset, Side.SELL, BigDecimal.ONE, today);
        order1.setId(101L);
        var order2 = TestUtils.order(customer, asset, Side.BUY, BigDecimal.valueOf(2), today);
        order2.setId(102L);

        when(orderRepository.findByCustomerIdAndCreateDateBetween(eq(customerId), any(), any()))
                .thenReturn(List.of(order1, order2));

        OrderListDto response = orderService.listOrders(customerId, today, today);

        assertNotNull(response);
        assertEquals(2, response.orders().size());

        OrderDetailDto first = response.orders().getFirst();
        assertEquals(101L, first.id());
        assertEquals("GPU", first.asset().assetName());
        assertEquals(Side.SELL, first.side());
        assertEquals(Status.PENDING, first.status());
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
    void findOrder_shouldReturnOrderDetailDto_whenExists() {
        var asset = TestUtils.asset("Keyboard", BigDecimal.ONE, BigDecimal.valueOf(100));
        var customer = TestUtils.customer("User", "Dev");
        var order = TestUtils.order(customer, asset, Side.SELL, BigDecimal.ONE, new Date());
        order.setId(99L);
        order.setTryCount(2); // to check mapping

        when(orderRepository.findById(99L)).thenReturn(Optional.of(order));

        OrderDetailDto dto = orderService.findOrder(99L);

        assertNotNull(dto);
        assertEquals(99L, dto.id());
        assertEquals(Side.SELL, dto.side());
        assertEquals(Status.PENDING, dto.status());
        assertEquals(BigDecimal.ONE, dto.size());
        assertEquals(2, dto.tryCount());
        assertEquals(customer.getId(), dto.customer().id());
        assertEquals(customer.getName(), dto.customer().name());
        assertEquals(customer.getSurname(), dto.customer().surname());
        assertEquals(asset.getId(), dto.asset().id());
        assertEquals(asset.getAssetName(), dto.asset().assetName());
    }

    @Test
    void findOrder_shouldThrow_whenNotFound() {
        when(orderRepository.findById(88L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> orderService.findOrder(88L));
    }
}
