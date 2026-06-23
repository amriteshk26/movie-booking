package com.amritesh.moviebooking.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request to place a time-bound hold on a set of seats for a show.
 * {@code showSeatIds} are the ids of {@code ShowSeat} rows for the show.
 */
public record HoldRequest(
        @NotNull Long showId,
        @NotEmpty List<Long> showSeatIds
) {
}
