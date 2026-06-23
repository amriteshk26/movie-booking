package com.amritesh.moviebooking.dto.request;

import com.amritesh.moviebooking.entity.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DiscountCodeRequest(
        @NotBlank String code,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin("0.0") BigDecimal value,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        @Positive Integer maxUses
) {
}
