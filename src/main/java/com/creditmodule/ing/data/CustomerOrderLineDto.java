package com.creditmodule.ing.data;

import com.creditmodule.ing.enums.Side;
import com.creditmodule.ing.enums.Status;

import java.math.BigDecimal;
import java.util.Date;

public record CustomerOrderLineDto(Long orderId,
                                   Long assetId,
                                   String assetName,
                                   Side side,
                                   Status status,
                                   BigDecimal size,
                                   Date createDate,
                                   int tryCount) {
}