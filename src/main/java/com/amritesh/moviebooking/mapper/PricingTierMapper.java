package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.PricingTierResponse;
import com.amritesh.moviebooking.entity.PricingTier;
import org.springframework.stereotype.Component;

@Component
public class PricingTierMapper {

    public PricingTierResponse toResponse(PricingTier tier) {
        return new PricingTierResponse(
                tier.getId(),
                tier.getTierType(),
                tier.getBasePrice(),
                tier.getPremiumMultiplier()
        );
    }
}
