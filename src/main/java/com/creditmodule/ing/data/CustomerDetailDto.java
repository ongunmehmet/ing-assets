package com.creditmodule.ing.data;

import java.math.BigDecimal;
import java.util.List;

public record CustomerDetailDto(
        Long id,
        String name,
        String surname,
        BigDecimal credit,
        List<CustomerAssetLineDto> assets,
        List<CustomerOrderLineDto> orders
) {}