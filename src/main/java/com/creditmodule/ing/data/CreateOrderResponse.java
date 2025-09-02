package com.creditmodule.ing.data;

import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResponse {
    private Long orderId;
    private String assetName;
    private Side orderSide;
    private Status status;
    private double size;
    private double price;
    private Date createDate;
    private String message;
}