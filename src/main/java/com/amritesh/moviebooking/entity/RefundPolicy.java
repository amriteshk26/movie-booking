package com.amritesh.moviebooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configurable refund policy. Refund percentage is tiered by how many hours
 * before show start the cancellation occurs:
 *  - cancel >= fullRefundHoursBefore  -> fullRefundPercent
 *  - cancel >= partialRefundHoursBefore -> partialRefundPercent
 *  - otherwise                        -> noRefundPercent (typically 0)
 */
@Entity
@Table(name = "refund_policies")
@Getter
@Setter
@NoArgsConstructor
public class RefundPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer fullRefundHoursBefore;

    @Column(nullable = false)
    private Integer partialRefundHoursBefore;

    @Column(nullable = false)
    private Integer fullRefundPercent = 100;

    @Column(nullable = false)
    private Integer partialRefundPercent = 50;

    @Column(nullable = false)
    private Integer noRefundPercent = 0;

    /** Whether this is the default policy applied when a show has none set. */
    @Column(nullable = false)
    private boolean isDefault = false;
}
