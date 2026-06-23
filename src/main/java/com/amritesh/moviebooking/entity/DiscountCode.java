package com.amritesh.moviebooking.entity;

import com.amritesh.moviebooking.entity.enums.DiscountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discount_codes")
@Getter
@Setter
@NoArgsConstructor
public class DiscountCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    /** Percent (0-100) when PERCENT, or flat currency amount when FLAT. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    /** Null means unlimited. */
    private Integer maxUses;

    @Column(nullable = false)
    private Integer usedCount = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private Long version;
}
