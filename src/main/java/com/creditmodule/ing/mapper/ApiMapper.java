package com.creditmodule.ing.mapper;


import com.creditmodule.ing.data.AssetCustomerLineDto;
import com.creditmodule.ing.data.AssetDetailDto;
import com.creditmodule.ing.data.CustomerAssetLineDto;
import com.creditmodule.ing.data.CustomerDetailDto;
import com.creditmodule.ing.data.CustomerOrderLineDto;
import com.creditmodule.ing.data.OrderAssetLineDto;
import com.creditmodule.ing.data.OrderCustomerLineDto;
import com.creditmodule.ing.data.OrderDetailDto;
import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.CustomerAsset;
import com.creditmodule.ing.entity.Order;

import java.util.List;

public final class ApiMapper {
    private ApiMapper() {
    }


    public static CustomerDetailDto toCustomerDetail(Customer c) {
        List<CustomerAssetLineDto> assets = c.getCustomerAssets()
                .stream()
                .map(ApiMapper::toCustomerAssetLine)
                .toList();

        List<CustomerOrderLineDto> orders = c.getOrders()
                .stream()
                .map(ApiMapper::toCustomerOrderLine)
                .toList();

        return new CustomerDetailDto(
                c.getId(),
                c.getName(),
                c.getSurname(),
                c.getCredit(),
                assets,
                orders
        );
    }

    private static CustomerAssetLineDto toCustomerAssetLine(CustomerAsset ca) {
        return new CustomerAssetLineDto(
                ca.getAsset().getId(),
                ca.getAsset().getAssetName(),
                ca.getSize(),
                ca.getUsableSize()
        );
    }

    private static CustomerOrderLineDto toCustomerOrderLine(Order o) {
        return new CustomerOrderLineDto(
                o.getId(),
                o.getAsset().getId(),
                o.getAsset().getAssetName(),
                o.getOrderSide(),
                o.getStatus(),
                o.getSize(),
                o.getCreateDate(),
                o.getTryCount()
        );
    }

    public static AssetDetailDto toAssetDetail(Asset a) {
        List<AssetCustomerLineDto> holders = a.getCustomerAssets()
                .stream()
                .map(ApiMapper::toAssetCustomerLine)
                .toList();

        return new AssetDetailDto(
                a.getId(),
                a.getAssetName(),
                a.getSize(),
                a.getUsableSize(),
                a.getInitialPrice(),
                holders
        );
    }

    private static AssetCustomerLineDto toAssetCustomerLine(CustomerAsset ca) {
        Customer c = ca.getCustomer();
        return new AssetCustomerLineDto(
                c.getId(),
                c.getName(),
                c.getSurname(),
                ca.getSize(),
                ca.getUsableSize()
        );
    }

    public static OrderDetailDto toOrderDetail(Order o) {
        OrderCustomerLineDto customer = new OrderCustomerLineDto(
                o.getCustomer().getId(),
                o.getCustomer().getName(),
                o.getCustomer().getSurname()
        );

        OrderAssetLineDto asset = new OrderAssetLineDto(
                o.getAsset().getId(),
                o.getAsset().getAssetName()
        );

        return new OrderDetailDto(
                o.getId(),
                o.getOrderSide(),
                o.getStatus(),
                o.getSize(),
                o.getCreateDate(),
                o.getTryCount(),
                customer,
                asset
        );
    }
}