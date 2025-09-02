package com.creditmodule.ing.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAssetResponse {
    private Long assetId;
    private String assetName;
    private double totalSize;
    private double usableSize;
}
