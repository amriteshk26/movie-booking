package com.amritesh.moviebooking.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CityRequest(
        @NotBlank String name,
        @NotBlank String state
) {
}
