package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.ShowSeatResponse;
import com.amritesh.moviebooking.entity.Seat;
import com.amritesh.moviebooking.entity.ShowSeat;
import org.springframework.stereotype.Component;

@Component
public class ShowSeatMapper {

    public ShowSeatResponse toResponse(ShowSeat showSeat) {
        Seat seat = showSeat.getSeat();
        return new ShowSeatResponse(
                showSeat.getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getSeatType(),
                showSeat.getStatus(),
                showSeat.getPrice()
        );
    }
}
