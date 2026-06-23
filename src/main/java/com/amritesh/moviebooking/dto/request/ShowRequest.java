package com.amritesh.moviebooking.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ShowRequest(
        @NotNull Long movieId,
        @NotNull Long screenId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @NotNull Long pricingTierId,
        Long refundPolicyId
) {
}
