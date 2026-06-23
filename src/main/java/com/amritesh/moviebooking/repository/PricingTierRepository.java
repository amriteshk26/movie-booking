package com.amritesh.moviebooking.repository;

import com.amritesh.moviebooking.entity.PricingTier;
import com.amritesh.moviebooking.entity.enums.TierType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PricingTierRepository extends JpaRepository<PricingTier, Long> {

    Optional<PricingTier> findByTierType(TierType tierType);
}
