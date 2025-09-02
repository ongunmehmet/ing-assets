package com.creditmodule.ing.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "customer_assets")
public class CustomerAsset {

    @EmbeddedId
    private CustomerAssetId id = new CustomerAssetId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("customerId")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("assetId")
    private Asset asset;

    private double size;        // how much this customer owns
    private double usableSize;  // free to trade
}
