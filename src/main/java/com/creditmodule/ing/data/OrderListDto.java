package com.creditmodule.ing.data;

import java.util.List;

public record OrderListDto(
        List<OrderDetailDto> orders
) {
}