package com.creditmodule.ing.data;

import java.math.BigDecimal;

public record CustomerAssetLineDto(Long assetId,
                                   String assetName,
                                   BigDecimal size,
                                   BigDecimal usableSize) {
}