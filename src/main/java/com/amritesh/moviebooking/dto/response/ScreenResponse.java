package com.amritesh.moviebooking.dto.response;

public record ScreenResponse(
        Long id,
        String name,
        Long theaterId,
        String theaterName
) {
}
