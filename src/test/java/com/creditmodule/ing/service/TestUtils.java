package com.creditmodule.ing.service;



import com.creditmodule.ing.data.CreateAssetRequest;
import com.creditmodule.ing.data.CreateOrderRequest;
import com.creditmodule.ing.data.UserCustomerCreateRequest;
import com.creditmodule.ing.entity.*;

import com.creditmodule.ing.enums.Role;
import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TestUtils {

    public static User createMockUser() {
        User user = new User();
        user.setAccountNumber(UUID.randomUUID().toString());
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRoles(Set.of(Role.ADMIN));
        return user;
    }

    public static Customer createMockCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Alice");
        customer.setSurname("Smith");
        customer.setCredit(BigDecimal.valueOf(10_000));
        customer.setUser(createMockUser());
        return customer;
    }

    public static Asset createMockAsset() {
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setAssetName("AAPL");
        asset.setInitialPrice(BigDecimal.valueOf(100.0));
        asset.setSize(BigDecimal.valueOf(1000.0));
        asset.setUsableSize(BigDecimal.valueOf(1000.0));
        return asset;
    }

    public static CustomerAsset createMockCustomerAsset(Customer customer, Asset asset) {
        CustomerAsset customerAsset = new CustomerAsset();
        CustomerAssetId id = new CustomerAssetId();
        id.setCustomerId(customer.getId());
        id.setAssetId(asset.getId());

        customerAsset.setId(id);
        customerAsset.setCustomer(customer);
        customerAsset.setAsset(asset);
        customerAsset.setSize(BigDecimal.valueOf(50.0));
        customerAsset.setUsableSize(BigDecimal.valueOf(50.0));
        return customerAsset;
    }

    public static Order createMockBuyOrder(Customer customer, Asset asset) {
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setAsset(asset);
        order.setOrderSide(Side.BUY);
        order.setStatus(Status.PENDING);
        order.setSize(BigDecimal.valueOf(10.0));
        order.setCreateDate(new Date());
        order.setTryCount(0);
        return order;
    }

    public static Order createMockSellOrder(Customer customer, Asset asset) {
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setAsset(asset);
        order.setOrderSide(Side.SELL);
        order.setStatus(Status.PENDING);
        order.setSize(BigDecimal.valueOf(10.0));
        order.setCreateDate(new Date());
        order.setTryCount(0);
        return order;
    }

    public static CreateAssetRequest createMockAssetRequest() {
        CreateAssetRequest request = new CreateAssetRequest();
        request.setAssetName("GOOGL");
        request.setInitialSize(BigDecimal.valueOf(500.0));
        return request;
    }

    public static CreateOrderRequest createMockOrderRequest(Customer customer, Asset asset) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(customer.getId());
        request.setAssetName(asset.getAssetName());
        request.setSide(Side.BUY);
        request.setSize(BigDecimal.valueOf(5.0));
        return request;
    }

    public static UserCustomerCreateRequest createMockUserCustomerCreateRequest() {
        UserCustomerCreateRequest request = new UserCustomerCreateRequest();
        request.setName("Test");
        request.setSurname("User");
        request.setPassword("password123");
        request.setCreditLimit(BigDecimal.valueOf(10_000));
        return request;
    }

    public static CreateAssetRequest createAssetRequest(String name, BigDecimal size, BigDecimal price) {
        var req = new CreateAssetRequest();
        req.setAssetName(name);
        req.setInitialSize(size);
        req.setInitialPrice(price);
        return req;
    }

    public static Asset asset(String name, BigDecimal size, BigDecimal price) {
        var asset = new Asset();
        asset.setId(ThreadLocalRandom.current().nextLong(1, 100));
        asset.setAssetName(name);
        asset.setSize(size);
        asset.setUsableSize(size); // full usable by default
        asset.setInitialPrice(price);
        return asset;
    }

    public static Customer customer(String name, String surname) {
        var customer = new Customer();
        customer.setId(ThreadLocalRandom.current().nextLong(1, 100));
        customer.setName(name);
        customer.setSurname(surname);
        customer.setCredit(BigDecimal.valueOf(10_000));
        customer.setUsedCredit(BigDecimal.ZERO);
        return customer;
    }

    public static CustomerAsset customerAsset(Customer customer, Asset asset, BigDecimal size, BigDecimal usableSize) {
        var ca = new CustomerAsset();
        var id = new CustomerAssetId();
        id.setCustomerId(customer.getId());
        id.setAssetId(asset.getId());

        ca.setId(id);
        ca.setCustomer(customer);
        ca.setAsset(asset);
        ca.setSize(size);
        ca.setUsableSize(usableSize);
        return ca;
    }

    public static CreateOrderRequest createOrderRequest(Long customerId, String assetName, Side side, BigDecimal size) {
        var req = new CreateOrderRequest();
        req.setCustomerId(customerId);
        req.setAssetName(assetName);
        req.setSide(side);
        req.setSize(size);
        return req;
    }

    public static Order order(Customer customer, Asset asset, Side side, BigDecimal size, Date date) {
        var order = new Order();
        order.setCustomer(customer);
        order.setAsset(asset);
        order.setOrderSide(side);
        order.setSize(size);
        order.setCreateDate(date);
        order.setStatus(Status.PENDING);
        return order;
    }
}



