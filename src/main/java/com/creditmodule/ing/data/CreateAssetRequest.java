package com.creditmodule.ing.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateAssetRequest {
    @NotBlank(message = "Asset name is required")
    private String assetName;

    @Positive(message = "Initial size must be greater than 0")
    private double initialSize;

    @Positive(message = "Initial price must be greater than 0")
    private double initialPrice;

}
