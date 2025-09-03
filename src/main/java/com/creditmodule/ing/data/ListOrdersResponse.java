package com.creditmodule.ing.data;

import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListOrdersResponse {
    private List<OrderDto> orders;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderDto {
        private Long orderId;
        private String assetName;
        private Side orderSide;
        private Status status;
        private double size;
        private Date createDate;
    }
}
