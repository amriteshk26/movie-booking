package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.TheaterResponse;
import com.amritesh.moviebooking.entity.Theater;
import org.springframework.stereotype.Component;

@Component
public class TheaterMapper {

    public TheaterResponse toResponse(Theater theater) {
        return new TheaterResponse(
                theater.getId(),
                theater.getName(),
                theater.getAddress(),
                theater.getCity().getId(),
                theater.getCity().getName()
        );
    }
}
