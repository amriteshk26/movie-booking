package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.ScreenResponse;
import com.amritesh.moviebooking.entity.Screen;
import org.springframework.stereotype.Component;

@Component
public class ScreenMapper {

    public ScreenResponse toResponse(Screen screen) {
        return new ScreenResponse(
                screen.getId(),
                screen.getName(),
                screen.getTheater().getId(),
                screen.getTheater().getName()
        );
    }
}
