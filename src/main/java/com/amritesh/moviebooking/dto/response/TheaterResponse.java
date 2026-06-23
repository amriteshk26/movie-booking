package com.amritesh.moviebooking.dto.response;

public record TheaterResponse(
        Long id,
        String name,
        String address,
        Long cityId,
        String cityName
) {
}
