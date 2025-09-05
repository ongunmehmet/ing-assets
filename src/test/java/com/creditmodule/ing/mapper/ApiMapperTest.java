package com.creditmodule.ing.mapper;


import com.creditmodule.ing.data.AssetCustomerLineDto;
import com.creditmodule.ing.data.AssetDetailDto;
import com.creditmodule.ing.data.CustomerAssetLineDto;
import com.creditmodule.ing.data.CustomerDetailDto;
import com.creditmodule.ing.data.CustomerOrderLineDto;
import com.creditmodule.ing.data.OrderDetailDto;
import com.creditmodule.ing.entity.Asset;
import com.creditmodule.ing.entity.Customer;
import com.creditmodule.ing.entity.CustomerAsset;
import com.creditmodule.ing.entity.CustomerAssetId;
import com.creditmodule.ing.entity.Order;
import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;
import com.creditmodule.ing.service.TestUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiMapperTest {

    @Test
    void toCustomerDetail_mapsAssetsAndOrders_flat_no_recursion() {

        Customer c = TestUtils.customer("Alice", "Smith");
        c.setId(1L);
        c.setCredit(new BigDecimal("9000"));


        Asset aapl = TestUtils.asset("AAPL", new BigDecimal("100"), new BigDecimal("150"));
        aapl.setId(10L);
        Asset msft = TestUtils.asset("MSFT", new BigDecimal("50"), new BigDecimal("300"));
        msft.setId(11L);

        CustomerAsset ca1 = link(c, aapl, "5", "3");
        CustomerAsset ca2 = link(c, msft, "2", "2");
        c.setCustomerAssets(List.of(ca1, ca2));


        Order o1 = TestUtils.order(c, aapl, Side.BUY, new BigDecimal("1"), new Date());
        o1.setId(100L);
        o1.setStatus(Status.PENDING);

        Order o2 = TestUtils.order(c, msft, Side.SELL, new BigDecimal("2"), new Date());
        o2.setId(101L);
        o2.setStatus(Status.MATCHED);

        c.setOrders(List.of(o1, o2));


        CustomerDetailDto dto = ApiMapper.toCustomerDetail(c);


        assertEquals(1L, dto.id());
        assertEquals("Alice", dto.name());
        assertEquals("Smith", dto.surname());
        assertEquals(0, dto.credit().compareTo(new BigDecimal("9000")));


        assertEquals(2, dto.assets().size());
        CustomerAssetLineDto aaplLine = dto.assets().stream()
                .filter(a -> a.assetName().equals("AAPL")).findFirst().orElseThrow();
        assertEquals(10L, aaplLine.assetId());
        assertEquals(0, aaplLine.size().compareTo(new BigDecimal("5")));
        assertEquals(0, aaplLine.usableSize().compareTo(new BigDecimal("3")));


        assertEquals(2, dto.orders().size());
        CustomerOrderLineDto buyLine = dto.orders().stream()
                .filter(o -> o.side() == Side.BUY).findFirst().orElseThrow();
        assertEquals(100L, buyLine.orderId());
        assertEquals("AAPL", buyLine.assetName());
        assertEquals(Status.PENDING, buyLine.status());
        assertNotNull(buyLine.createDate());
    }

    @Test
    void toAssetDetail_mapsHolders_flat_no_recursion() {
        Asset asset = TestUtils.asset("AAPL", new BigDecimal("100"), new BigDecimal("150"));
        asset.setId(10L);
        asset.setUsableSize(new BigDecimal("80"));

        Customer c1 = TestUtils.customer("Alice", "Smith"); c1.setId(1L);
        Customer c2 = TestUtils.customer("Bob", "Lee");     c2.setId(2L);

        CustomerAsset ca1 = link(c1, asset, "5", "5");
        CustomerAsset ca2 = link(c2, asset, "2", "1");
        asset.setCustomerAssets(List.of(ca1, ca2));

        AssetDetailDto dto = ApiMapper.toAssetDetail(asset);

        assertEquals(10L, dto.id());
        assertEquals("AAPL", dto.assetName());
        assertEquals(0, dto.size().compareTo(new BigDecimal("100")));
        assertEquals(0, dto.usableSize().compareTo(new BigDecimal("80")));
        assertEquals(0, dto.initialPrice().compareTo(new BigDecimal("150")));

        assertEquals(2, dto.holders().size());
        AssetCustomerLineDto holder1 = dto.holders().stream()
                .filter(h -> h.customerId().equals(1L)).findFirst().orElseThrow();
        assertEquals("Alice", holder1.customerName());
        assertEquals("Smith", holder1.customerSurname());
        assertEquals(0, holder1.size().compareTo(new BigDecimal("5")));
        assertEquals(0, holder1.usableSize().compareTo(new BigDecimal("5")));
    }

    @Test
    void toOrderDetail_mapsFlatCustomerAndAsset() {
        Customer customer = TestUtils.customer("Alice", "Smith"); customer.setId(1L);
        Asset asset = TestUtils.asset("AAPL", new BigDecimal("100"), new BigDecimal("150")); asset.setId(10L);

        Order order = TestUtils.order(customer, asset, Side.BUY, new BigDecimal("3"), new Date());
        order.setId(999L);
        order.setStatus(Status.PENDING);
        order.setTryCount(2);

        OrderDetailDto dto = ApiMapper.toOrderDetail(order);

        assertEquals(999L, dto.id());
        assertEquals(Side.BUY, dto.side());
        assertEquals(Status.PENDING, dto.status());
        assertEquals(0, dto.size().compareTo(new BigDecimal("3")));
        assertEquals(2, dto.tryCount());
        assertNotNull(dto.createDate());

        assertEquals(1L, dto.customer().id());
        assertEquals("Alice", dto.customer().name());
        assertEquals("Smith", dto.customer().surname());

        assertEquals(10L, dto.asset().id());
        assertEquals("AAPL", dto.asset().assetName());
    }

    private static CustomerAsset link(Customer c, Asset a, String size, String usable) {
        var ca = new CustomerAsset();
        var id = new CustomerAssetId();
        id.setCustomerId(c.getId());
        id.setAssetId(a.getId());
        ca.setId(id);
        ca.setCustomer(c);
        ca.setAsset(a);
        ca.setSize(new BigDecimal(size));
        ca.setUsableSize(new BigDecimal(usable));
        return ca;
    }
}
