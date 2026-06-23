package com.amritesh.moviebooking.dto.response;

public record MovieResponse(
        Long id,
        String title,
        String language,
        Integer durationMins,
        String certification
) {
}
