package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.entity.Booking;
import com.amritesh.moviebooking.entity.Payment;
import com.amritesh.moviebooking.entity.enums.PaymentStatus;
import com.amritesh.moviebooking.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock payment processor. No external provider is used (per project
 * constraints); charges always succeed and produce a synthetic transaction
 * reference.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment charge(Booking booking, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionRef("TXN-" + UUID.randomUUID());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refund(Payment payment, BigDecimal refundAmount) {
        payment.setRefundedAmount(refundAmount);
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).orElse(null);
    }
}
