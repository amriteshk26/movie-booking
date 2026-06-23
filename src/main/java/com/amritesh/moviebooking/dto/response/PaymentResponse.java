package com.amritesh.moviebooking.dto.response;

import com.amritesh.moviebooking.entity.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        Long id,
        BigDecimal amount,
        PaymentStatus status,
        String transactionRef,
        BigDecimal refundedAmount
) {
}
