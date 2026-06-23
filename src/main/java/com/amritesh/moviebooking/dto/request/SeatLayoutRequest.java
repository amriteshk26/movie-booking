package com.amritesh.moviebooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Bulk-creates the physical seat layout for a screen.
 */
public record SeatLayoutRequest(
        @NotNull Long screenId,
        @NotEmpty @Valid List<SeatRowSpec> rows
) {
}
