package com.amritesh.moviebooking.dto.response;

import com.amritesh.moviebooking.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        Long showId,
        String movieTitle,
        LocalDateTime showStartTime,
        BookingStatus status,
        List<ShowSeatResponse> seats,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String discountCode,
        PaymentResponse payment,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt
) {
}
