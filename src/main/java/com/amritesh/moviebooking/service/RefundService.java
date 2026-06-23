package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.entity.RefundPolicy;
import com.amritesh.moviebooking.entity.Show;
import com.amritesh.moviebooking.repository.RefundPolicyRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Computes refund amounts on cancellation according to the show's (or default)
 * configurable refund policy, tiered by how far ahead of show time the
 * cancellation occurs.
 */
@Service
public class RefundService {

    private final RefundPolicyRepository refundPolicyRepository;

    public RefundService(RefundPolicyRepository refundPolicyRepository) {
        this.refundPolicyRepository = refundPolicyRepository;
    }

    public record RefundResult(int percent, BigDecimal amount) {
    }

    public RefundResult computeRefund(Show show, BigDecimal paidAmount) {
        RefundPolicy policy = resolvePolicy(show);
        int percent = resolvePercent(policy, show.getStartTime());
        BigDecimal amount = paidAmount
                .multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new RefundResult(percent, amount);
    }

    private RefundPolicy resolvePolicy(Show show) {
        if (show.getRefundPolicy() != null) {
            return show.getRefundPolicy();
        }
        return refundPolicyRepository.findByIsDefaultTrue().orElse(null);
    }

    private int resolvePercent(RefundPolicy policy, LocalDateTime showStart) {
        // No policy configured -> no refund (safe default).
        if (policy == null) {
            return 0;
        }
        long hoursBefore = Duration.between(LocalDateTime.now(), showStart).toHours();
        if (hoursBefore < 0) {
            // Show already started.
            return policy.getNoRefundPercent();
        }
        if (hoursBefore >= policy.getFullRefundHoursBefore()) {
            return policy.getFullRefundPercent();
        }
        if (hoursBefore >= policy.getPartialRefundHoursBefore()) {
            return policy.getPartialRefundPercent();
        }
        return policy.getNoRefundPercent();
    }
}
