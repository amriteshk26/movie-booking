package com.amritesh.moviebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TheaterRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotNull Long cityId
) {
}
