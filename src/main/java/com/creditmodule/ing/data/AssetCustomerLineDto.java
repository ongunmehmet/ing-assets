package com.creditmodule.ing.data;

import java.math.BigDecimal;

public record AssetCustomerLineDto(Long customerId,
                                   String customerName,
                                   String customerSurname,
                                   BigDecimal size,
                                   BigDecimal usableSize) {
}