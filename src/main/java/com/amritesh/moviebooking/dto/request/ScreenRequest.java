package com.amritesh.moviebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScreenRequest(
        @NotBlank String name,
        @NotNull Long theaterId
) {
}
