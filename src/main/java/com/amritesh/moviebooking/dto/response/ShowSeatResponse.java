package com.amritesh.moviebooking.dto.response;

import com.amritesh.moviebooking.entity.enums.SeatType;
import com.amritesh.moviebooking.entity.enums.ShowSeatStatus;

import java.math.BigDecimal;

public record ShowSeatResponse(
        Long id,
        String rowLabel,
        Integer seatNumber,
        SeatType seatType,
        ShowSeatStatus status,
        BigDecimal price
) {
}
