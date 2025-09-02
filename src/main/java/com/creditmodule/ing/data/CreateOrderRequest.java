package com.creditmodule.ing.data;

import com.creditmodule.ing.enums.Side;
import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long customerId;      // Customer placing the order
    private String assetName;     // Name of the asset
    private Side side;            // BUY or SELL
    private double size;          // Quantity of asset
    private double price;         // Price per share
}

