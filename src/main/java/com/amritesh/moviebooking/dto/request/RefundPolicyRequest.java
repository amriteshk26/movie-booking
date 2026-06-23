package com.amritesh.moviebooking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RefundPolicyRequest(
        @NotBlank String name,
        @NotNull @PositiveOrZero Integer fullRefundHoursBefore,
        @NotNull @PositiveOrZero Integer partialRefundHoursBefore,
        @NotNull @Min(0) @Max(100) Integer fullRefundPercent,
        @NotNull @Min(0) @Max(100) Integer partialRefundPercent,
        @NotNull @Min(0) @Max(100) Integer noRefundPercent,
        boolean isDefault
) {
}
