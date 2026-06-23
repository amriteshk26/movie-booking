package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.entity.PricingTier;
import com.amritesh.moviebooking.entity.enums.SeatType;
import com.amritesh.moviebooking.entity.enums.TierType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService();

    private PricingTier tier(String base, String multiplier) {
        PricingTier tier = new PricingTier();
        tier.setTierType(TierType.REGULAR);
        tier.setBasePrice(new BigDecimal(base));
        tier.setPremiumMultiplier(new BigDecimal(multiplier));
        return tier;
    }

    @Test
    void regularSeatUsesBasePrice() {
        BigDecimal price = pricingService.priceFor(tier("200.00", "1.50"), SeatType.REGULAR);
        assertThat(price).isEqualByComparingTo("200.00");
        assertThat(price.scale()).isEqualTo(2);
    }

    @Test
    void premiumSeatAppliesMultiplier() {
        BigDecimal price = pricingService.priceFor(tier("200.00", "1.50"), SeatType.PREMIUM);
        assertThat(price).isEqualByComparingTo("300.00");
    }

    @Test
    void premiumPriceIsRoundedToTwoDecimals() {
        // 199.99 * 1.5 = 299.985 -> 299.99 (HALF_UP)
        BigDecimal price = pricingService.priceFor(tier("199.99", "1.50"), SeatType.PREMIUM);
        assertThat(price).isEqualByComparingTo("299.99");
        assertThat(price.scale()).isEqualTo(2);
    }
}
