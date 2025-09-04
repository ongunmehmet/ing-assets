package com.creditmodule.ing.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAssetResponse {
    private Long assetId;
    private String assetName;
    private BigDecimal totalSize;
    private BigDecimal usableSize;
}
