package com.creditmodule.ing.data;

import java.math.BigDecimal;
import java.util.List;

public record AssetDetailDto(Long id,
                             String assetName,
                             BigDecimal size,
                             BigDecimal usableSize,
                             BigDecimal initialPrice,
                             List<AssetCustomerLineDto> holders) {
}