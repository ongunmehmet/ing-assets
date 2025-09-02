package com.creditmodule.ing.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class CustomerAssetId implements Serializable {
    private Long customerId;
    private Long assetId;
}
