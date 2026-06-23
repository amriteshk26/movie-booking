package com.amritesh.moviebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record MovieRequest(
        @NotBlank String title,
        String language,
        @Positive Integer durationMins,
        String certification
) {
}
