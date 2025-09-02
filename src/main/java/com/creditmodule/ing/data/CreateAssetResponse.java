package com.creditmodule.ing.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAssetResponse {
    private Long id;
    private String assetName;
    private double size;
    private double usableSize;
    private String message;
}
