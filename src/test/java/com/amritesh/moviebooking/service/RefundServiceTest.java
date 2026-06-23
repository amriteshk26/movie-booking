package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.entity.RefundPolicy;
import com.amritesh.moviebooking.entity.Show;
import com.amritesh.moviebooking.repository.RefundPolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundPolicyRepository refundPolicyRepository;
    @InjectMocks
    private RefundService refundService;

    private RefundPolicy standardPolicy() {
        RefundPolicy policy = new RefundPolicy();
        policy.setName("Standard");
        policy.setFullRefundHoursBefore(48);
        policy.setPartialRefundHoursBefore(24);
        policy.setFullRefundPercent(100);
        policy.setPartialRefundPercent(50);
        policy.setNoRefundPercent(0);
        return policy;
    }

    private Show showStartingInHours(long hours, RefundPolicy policy) {
        Show show = new Show();
        show.setStartTime(LocalDateTime.now().plusHours(hours));
        show.setRefundPolicy(policy);
        return show;
    }

    @Test
    void fullRefundWhenCancelledWellAhead() {
        Show show = showStartingInHours(72, standardPolicy());
        var result = refundService.computeRefund(show, new BigDecimal("1000.00"));
        assertThat(result.percent()).isEqualTo(100);
        assertThat(result.amount()).isEqualByComparingTo("1000.00");
    }

    @Test
    void partialRefundWithinPartialWindow() {
        Show show = showStartingInHours(30, standardPolicy());
        var result = refundService.computeRefund(show, new BigDecimal("1000.00"));
        assertThat(result.percent()).isEqualTo(50);
        assertThat(result.amount()).isEqualByComparingTo("500.00");
    }

    @Test
    void noRefundCloseToShowtime() {
        Show show = showStartingInHours(5, standardPolicy());
        var result = refundService.computeRefund(show, new BigDecimal("1000.00"));
        assertThat(result.percent()).isEqualTo(0);
        assertThat(result.amount()).isEqualByComparingTo("0.00");
    }

    @Test
    void fallsBackToDefaultPolicyWhenShowHasNone() {
        Show show = showStartingInHours(72, null);
        when(refundPolicyRepository.findByIsDefaultTrue()).thenReturn(Optional.of(standardPolicy()));
        var result = refundService.computeRefund(show, new BigDecimal("1000.00"));
        assertThat(result.percent()).isEqualTo(100);
    }

    @Test
    void noPolicyMeansNoRefund() {
        Show show = showStartingInHours(72, null);
        when(refundPolicyRepository.findByIsDefaultTrue()).thenReturn(Optional.empty());
        var result = refundService.computeRefund(show, new BigDecimal("1000.00"));
        assertThat(result.percent()).isEqualTo(0);
        assertThat(result.amount()).isEqualByComparingTo("0.00");
    }
}
