package com.amritesh.moviebooking.dto.response;

import com.amritesh.moviebooking.entity.enums.TierType;

import java.time.LocalDateTime;

public record ShowResponse(
        Long id,
        Long movieId,
        String movieTitle,
        Long screenId,
        String screenName,
        String theaterName,
        String cityName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        TierType tierType,
        Long refundPolicyId
) {
}
