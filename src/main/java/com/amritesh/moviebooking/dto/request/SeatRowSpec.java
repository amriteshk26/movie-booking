package com.amritesh.moviebooking.dto.request;

import com.amritesh.moviebooking.entity.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Specifies one row of a seat layout: seats numbered 1..seatCount of the given type.
 */
public record SeatRowSpec(
        @NotBlank String rowLabel,
        @NotNull @Positive Integer seatCount,
        @NotNull SeatType seatType
) {
}
