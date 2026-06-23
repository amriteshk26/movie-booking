package com.amritesh.moviebooking.dto.response;

import com.amritesh.moviebooking.entity.enums.BookingStatus;

import java.math.BigDecimal;

public record CancellationResponse(
        Long bookingId,
        BookingStatus status,
        Integer refundPercent,
        BigDecimal refundAmount,
        String message
) {
}
