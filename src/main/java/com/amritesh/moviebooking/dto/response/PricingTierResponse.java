package com.amritesh.moviebooking.dto.response;

import com.amritesh.moviebooking.entity.enums.TierType;

import java.math.BigDecimal;

public record PricingTierResponse(
        Long id,
        TierType tierType,
        BigDecimal basePrice,
        BigDecimal premiumMultiplier
) {
}
