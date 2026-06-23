package com.amritesh.moviebooking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record HoldResponse(
        Long holdId,
        Long showId,
        String status,
        LocalDateTime expiresAt,
        List<ShowSeatResponse> seats,
        BigDecimal totalPrice
) {
}
