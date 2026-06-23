package com.amritesh.moviebooking.entity;

import com.amritesh.moviebooking.entity.enums.TierType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Defines a price level. A show references a base tier; the seat type and tier
 * combine to produce the final per-seat price.
 */
@Entity
@Table(name = "pricing_tiers")
@Getter
@Setter
@NoArgsConstructor
public class PricingTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TierType tierType;

    /** Base price for a regular seat under this tier. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    /** Multiplier applied for premium seats (e.g. 1.5). */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal premiumMultiplier = BigDecimal.valueOf(1.5);
}
