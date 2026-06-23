package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.request.PricingTierRequest;
import com.amritesh.moviebooking.dto.response.PricingTierResponse;
import com.amritesh.moviebooking.entity.PricingTier;
import com.amritesh.moviebooking.exception.BadRequestException;
import com.amritesh.moviebooking.exception.ResourceNotFoundException;
import com.amritesh.moviebooking.mapper.PricingTierMapper;
import com.amritesh.moviebooking.repository.PricingTierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PricingTierService {

    private final PricingTierRepository pricingTierRepository;
    private final PricingTierMapper pricingTierMapper;

    public PricingTierService(PricingTierRepository pricingTierRepository,
                              PricingTierMapper pricingTierMapper) {
        this.pricingTierRepository = pricingTierRepository;
        this.pricingTierMapper = pricingTierMapper;
    }

    @Transactional
    public PricingTierResponse create(PricingTierRequest request) {
        pricingTierRepository.findByTierType(request.tierType()).ifPresent(t -> {
            throw new BadRequestException("Pricing tier already exists: " + request.tierType());
        });
        PricingTier tier = new PricingTier();
        tier.setTierType(request.tierType());
        tier.setBasePrice(request.basePrice());
        tier.setPremiumMultiplier(request.premiumMultiplier());
        return pricingTierMapper.toResponse(pricingTierRepository.save(tier));
    }

    @Transactional
    public PricingTierResponse update(Long id, PricingTierRequest request) {
        PricingTier tier = pricingTierRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("PricingTier", id));
        tier.setBasePrice(request.basePrice());
        tier.setPremiumMultiplier(request.premiumMultiplier());
        return pricingTierMapper.toResponse(tier);
    }

    @Transactional(readOnly = true)
    public List<PricingTierResponse> findAll() {
        return pricingTierRepository.findAll().stream().map(pricingTierMapper::toResponse).toList();
    }
}
