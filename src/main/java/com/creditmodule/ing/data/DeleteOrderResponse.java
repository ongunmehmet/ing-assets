package com.creditmodule.ing.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteOrderResponse {
    private Long orderId;
    private String assetName;
    private String message;
}
