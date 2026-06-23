package com.amritesh.moviebooking.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Confirms a held set of seats into a booking, optionally applying a discount code.
 * Triggers the mock payment.
 */
public record ConfirmBookingRequest(
        @NotNull Long holdId,
        String discountCode
) {
}
