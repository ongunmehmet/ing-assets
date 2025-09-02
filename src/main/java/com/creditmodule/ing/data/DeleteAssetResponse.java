package com.creditmodule.ing.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteAssetResponse {
    private Long id;
    private String assetName;
    private String message;
}
