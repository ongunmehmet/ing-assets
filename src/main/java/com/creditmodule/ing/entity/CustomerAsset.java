package com.creditmodule.ing.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "customer_assets")
public class CustomerAsset {

    @EmbeddedId
    private CustomerAssetId id = new CustomerAssetId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("customerId")
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("assetId")
    @JoinColumn(name = "asset_id")
    private Asset asset;

    private BigDecimal size;
    private BigDecimal usableSize;
}
