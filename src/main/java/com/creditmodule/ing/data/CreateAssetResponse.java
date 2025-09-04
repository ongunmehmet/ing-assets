package com.creditmodule.ing.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAssetResponse {
    private Long id;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;
    private String message;
}
