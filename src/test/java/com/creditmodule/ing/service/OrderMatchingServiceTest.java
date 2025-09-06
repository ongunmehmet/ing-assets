package com.creditmodule.ing.service;

import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.CustomerAsset;
import com.creditmodule.ing.entity.Order;
import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;
import com.creditmodule.ing.repository.AssetRepository;
import com.creditmodule.ing.repository.CustomerAssetRepository;
import com.creditmodule.ing.repository.CustomerRepository;
import com.creditmodule.ing.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OrderMatchingServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private CustomerAssetRepository customerAssetRepository;

    private OrderQueue orderQueue;
    private OrderMatchingService orderMatchingService;

    @BeforeEach
    void setUp() {
        orderQueue = new OrderQueue();
        orderMatchingService = new OrderMatchingService(
                orderRepository,
                assetRepository,
                customerRepository,
                customerAssetRepository,
                orderQueue
        );
    }

    @Test
    void processOrder_shouldMatchBuyOrder_andUpsertCustomerAsset() {
        Customer customer = TestUtils.customer("Test", "User");
        customer.setCredit(new BigDecimal("5000"));

        Asset asset = TestUtils.asset("Laptop", new BigDecimal("10"), new BigDecimal("1000"));
        Order order = TestUtils.order(customer, asset, Side.BUY, new BigDecimal("2"), new Date());
        order.setStatus(Status.PENDING);
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        // No existing holding → create new CustomerAsset
        when(customerAssetRepository.findByCustomerAndAsset(customer, asset))
                .thenReturn(Optional.empty());

        ArgumentCaptor<CustomerAsset> caCaptor = ArgumentCaptor.forClass(CustomerAsset.class);
        when(customerAssetRepository.save(caCaptor.capture()))
                .thenAnswer(inv -> inv.getArgument(0));

        orderMatchingService.processOrder(1L);

        assertEquals(Status.MATCHED, order.getStatus());
        assertEquals(0, asset.getUsableSize().compareTo(new BigDecimal("8")));
        assertEquals(0, customer.getCredit().compareTo(new BigDecimal("5000")));

        CustomerAsset saved = caCaptor.getValue();
        assertEquals(customer.getId(), saved.getCustomer().getId());
        assertEquals(asset.getId(), saved.getAsset().getId());
        assertEquals(0, saved.getSize().compareTo(new BigDecimal("2")));
        assertEquals(0, saved.getUsableSize().compareTo(new BigDecimal("2")));

        assertEquals(0, order.getTryCount());
        verify(orderRepository).save(order);
    }

    @Test
    void processOrder_shouldMatchSellOrder_whenHoldingSufficient_andUpdateHolding() {
        Customer customer = TestUtils.customer("Test", "User");
        customer.setCredit(new BigDecimal("1000"));

        Asset asset = TestUtils.asset("Laptop", new BigDecimal("10"), new BigDecimal("1000"));
        Order order = TestUtils.order(customer, asset, Side.SELL, BigDecimal.ONE, new Date());
        order.setStatus(Status.PENDING);
        order.setId(2L);

        CustomerAsset existing = TestUtils.customerAsset(
                customer, asset,
                new BigDecimal("3"),  // size
                new BigDecimal("2")   // usable
        );

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        when(customerAssetRepository.findByCustomerAndAsset(customer, asset))
                .thenReturn(Optional.of(existing));

        orderMatchingService.processOrder(2L);

        assertEquals(Status.MATCHED, order.getStatus());
        assertEquals(0, asset.getUsableSize().compareTo(new BigDecimal("11")));
        assertEquals(0, customer.getCredit().compareTo(new BigDecimal("2000"))); 

        // existing holding reduced by 1 (and usable by 1)
        assertEquals(0, existing.getSize().compareTo(new BigDecimal("2")));
        assertEquals(0, existing.getUsableSize().compareTo(new BigDecimal("1")));

        verify(customerAssetRepository).save(existing);
        verify(orderRepository).save(order);
    }

    @Test
    void processOrder_sellShouldFail_whenNoHolding_orInsufficientUsable() {
        Customer customer = TestUtils.customer("Test", "User");
        Asset asset = TestUtils.asset("GPU", new BigDecimal("5"), new BigDecimal("1000"));
        Order order = TestUtils.order(customer, asset, Side.SELL, new BigDecimal("2"), new Date());
        order.setStatus(Status.PENDING);
        order.setId(40L);
        order.setTryCount(0);

        when(orderRepository.findById(40L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        
        when(customerAssetRepository.findByCustomerAndAsset(customer, asset))
                .thenReturn(Optional.empty());

        orderMatchingService.processOrder(40L);

        assertEquals(1, order.getTryCount());
        assertEquals(Status.PENDING, order.getStatus());
        verify(orderRepository).save(order);
        verify(customerAssetRepository, never()).save(any());
    }

    @Test
    void processOrder_shouldRetry_onInsufficientSupply_forBuy() {
        Customer customer = TestUtils.customer("Test", "User");
        customer.setCredit(new BigDecimal("10000"));

        Asset asset = TestUtils.asset("GPU", new BigDecimal("1"), new BigDecimal("1000"));
        Order order = TestUtils.order(customer, asset, Side.BUY, new BigDecimal("2"), new Date()); // needs 2, have 1
        order.setStatus(Status.PENDING);
        order.setId(4L);
        order.setTryCount(2);

        when(orderRepository.findById(4L))
                .thenReturn(Optional.of(order))
                .thenReturn(Optional.of(order)); // service may re-read in failure path
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        // For BUY failure, holdings repo should not be touched
        orderMatchingService.processOrder(4L);

        assertEquals(3, order.getTryCount());
        assertEquals(Status.PENDING, order.getStatus());
        verify(orderRepository).save(order);
        verifyNoInteractions(customerAssetRepository);
    }

    @Test
    void processOrder_shouldCancelBuyAndRefund_onFifthFailure() {
        Customer customer = TestUtils.customer("Fail", "User");
        Asset asset = TestUtils.asset("Memory", new BigDecimal("0"), new BigDecimal("500"));
        Order order = TestUtils.order(customer, asset, Side.BUY, BigDecimal.ONE, new Date());
        order.setStatus(Status.PENDING);
        order.setId(5L);
        order.setTryCount(4);

        // Simulate upfront block at create-time (so refund is observable)
        BigDecimal blocked = asset.getInitialPrice().multiply(order.getSize());
        customer.setCredit(customer.getCredit().subtract(blocked));             

        when(orderRepository.findById(5L))
                .thenReturn(Optional.of(order))
                .thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processOrder(5L);

        assertEquals(Status.CANCELLED, order.getStatus());
        assertEquals(5, order.getTryCount());
        assertEquals(0, customer.getCredit().compareTo(new BigDecimal("10000"))); // refunded
        verify(orderRepository).save(order);
        verifyNoInteractions(customerAssetRepository);
    }

    @Test
    void processSingleOrder_shouldIncrementTryCount_onManualFailure() {
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
        assertEquals(Status.PENDING, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void processSingleOrder_shouldCancelAndRefund_onFifthManualFailure() {
        Customer customer = TestUtils.customer("Retry", "Limit");
        customer.setCredit(BigDecimal.ZERO);
        Asset asset = TestUtils.asset("RAM", new BigDecimal("1"), new BigDecimal("250"));
        Order order = TestUtils.order(customer, asset, Side.BUY, new BigDecimal("2"), new Date()); // refund = 500
        order.setStatus(Status.PENDING);
        order.setTryCount(4);
        order.setId(4L);

        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
        when(assetRepository.findByIdForUpdate(asset.getId())).thenReturn(asset);
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(customer);

        orderMatchingService.processSingleOrder(4L);

        assertEquals(Status.CANCELLED, order.getStatus());
        assertEquals(5, order.getTryCount());
        assertEquals(0, customer.getCredit().compareTo(new BigDecimal("500"))); // 0 + 500
        verify(orderRepository).save(order);
    }
}
