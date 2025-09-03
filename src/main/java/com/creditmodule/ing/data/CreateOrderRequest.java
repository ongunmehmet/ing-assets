package com.creditmodule.ing.data;

import com.creditmodule.ing.enums.Side;
import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long customerId;
    private String assetName;
    private Side side;
    private double size;
}

