package com.amritesh.moviebooking.dto.response;

public record RefundPolicyResponse(
        Long id,
        String name,
        Integer fullRefundHoursBefore,
        Integer partialRefundHoursBefore,
        Integer fullRefundPercent,
        Integer partialRefundPercent,
        Integer noRefundPercent,
        boolean isDefault
) {
}
