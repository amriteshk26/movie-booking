package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.SeatResponse;
import com.amritesh.moviebooking.entity.Seat;
import org.springframework.stereotype.Component;

@Component
public class SeatMapper {

    public SeatResponse toResponse(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getSeatType()
        );
    }
}
