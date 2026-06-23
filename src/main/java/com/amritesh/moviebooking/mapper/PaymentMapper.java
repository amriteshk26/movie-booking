package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.PaymentResponse;
import com.amritesh.moviebooking.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        return new PaymentResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionRef(),
                payment.getRefundedAmount()
        );
    }
}
