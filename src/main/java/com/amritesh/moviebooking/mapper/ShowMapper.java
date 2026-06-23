package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.ShowResponse;
import com.amritesh.moviebooking.entity.Screen;
import com.amritesh.moviebooking.entity.Show;
import org.springframework.stereotype.Component;

@Component
public class ShowMapper {

    public ShowResponse toResponse(Show show) {
        Screen screen = show.getScreen();
        return new ShowResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                screen.getId(),
                screen.getName(),
                screen.getTheater().getName(),
                screen.getTheater().getCity().getName(),
                show.getStartTime(),
                show.getEndTime(),
                show.getPricingTier().getTierType(),
                show.getRefundPolicy() != null ? show.getRefundPolicy().getId() : null
        );
    }
}
