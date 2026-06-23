package com.amritesh.moviebooking.dto.response;

import com.amritesh.moviebooking.entity.enums.SeatType;

public record SeatResponse(
        Long id,
        String rowLabel,
        Integer seatNumber,
        SeatType seatType
) {
}
