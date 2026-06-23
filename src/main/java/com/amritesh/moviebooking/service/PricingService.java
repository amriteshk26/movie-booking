package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.entity.PricingTier;
import com.amritesh.moviebooking.entity.enums.SeatType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Computes the final per-seat price from a show's pricing tier and the seat type.
 * Premium seats are scaled by the tier's premium multiplier.
 */
@Service
public class PricingService {

    public BigDecimal priceFor(PricingTier tier, SeatType seatType) {
        BigDecimal price = tier.getBasePrice();
        if (seatType == SeatType.PREMIUM) {
            price = price.multiply(tier.getPremiumMultiplier());
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
