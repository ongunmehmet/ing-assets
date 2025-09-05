package com.creditmodule.ing.data;

import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;

import java.math.BigDecimal;
import java.util.Date;

public record OrderDetailDto(Long id,
                             Side side,
                             Status status,
                             BigDecimal size,
                             Date createDate,
                             int tryCount,
                             OrderCustomerLineDto customer,
                             OrderAssetLineDto asset) {
}