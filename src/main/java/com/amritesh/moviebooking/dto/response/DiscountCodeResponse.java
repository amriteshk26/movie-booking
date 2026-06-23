package com.amritesh.moviebooking.dto.response;

import com.amritesh.moviebooking.entity.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DiscountCodeResponse(
        Long id,
        String code,
        DiscountType discountType,
        BigDecimal value,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        Integer maxUses,
        Integer usedCount,
        boolean active
) {
}
