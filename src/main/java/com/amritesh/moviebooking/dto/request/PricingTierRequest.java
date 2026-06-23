package com.amritesh.moviebooking.dto.request;

import com.amritesh.moviebooking.entity.enums.TierType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PricingTierRequest(
        @NotNull TierType tierType,
        @NotNull @DecimalMin("0.0") BigDecimal basePrice,
        @NotNull @DecimalMin("1.0") BigDecimal premiumMultiplier
) {
}
