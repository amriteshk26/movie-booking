package com.amritesh.moviebooking.controller;

import com.amritesh.moviebooking.dto.request.DiscountCodeRequest;
import com.amritesh.moviebooking.dto.request.PricingTierRequest;
import com.amritesh.moviebooking.dto.request.RefundPolicyRequest;
import com.amritesh.moviebooking.dto.response.DiscountCodeResponse;
import com.amritesh.moviebooking.dto.response.PricingTierResponse;
import com.amritesh.moviebooking.dto.response.RefundPolicyResponse;
import com.amritesh.moviebooking.service.DiscountService;
import com.amritesh.moviebooking.service.PricingTierService;
import com.amritesh.moviebooking.service.RefundPolicyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin management of pricing tiers, discount codes and refund policies.
 * Restricted to ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPricingController {

    private final PricingTierService pricingTierService;
    private final DiscountService discountService;
    private final RefundPolicyService refundPolicyService;

    public AdminPricingController(PricingTierService pricingTierService,
                                 DiscountService discountService,
                                 RefundPolicyService refundPolicyService) {
        this.pricingTierService = pricingTierService;
        this.discountService = discountService;
        this.refundPolicyService = refundPolicyService;
    }

    // ---------------- Pricing tiers ----------------

    @PostMapping("/pricing-tiers")
    public ResponseEntity<PricingTierResponse> createTier(@Valid @RequestBody PricingTierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingTierService.create(request));
    }

    @PutMapping("/pricing-tiers/{id}")
    public PricingTierResponse updateTier(@PathVariable Long id, @Valid @RequestBody PricingTierRequest request) {
        return pricingTierService.update(id, request);
    }

    @GetMapping("/pricing-tiers")
    public List<PricingTierResponse> tiers() {
        return pricingTierService.findAll();
    }

    // ---------------- Discount codes ----------------

    @PostMapping("/discount-codes")
    public ResponseEntity<DiscountCodeResponse> createDiscount(@Valid @RequestBody DiscountCodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(discountService.create(request));
    }

    @GetMapping("/discount-codes")
    public List<DiscountCodeResponse> discounts() {
        return discountService.findAll();
    }

    @PatchMapping("/discount-codes/{id}/active")
    public DiscountCodeResponse setDiscountActive(@PathVariable Long id, @RequestParam boolean active) {
        return discountService.setActive(id, active);
    }

    // ---------------- Refund policies ----------------

    @PostMapping("/refund-policies")
    public ResponseEntity<RefundPolicyResponse> createPolicy(@Valid @RequestBody RefundPolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(refundPolicyService.create(request));
    }

    @PutMapping("/refund-policies/{id}")
    public RefundPolicyResponse updatePolicy(@PathVariable Long id, @Valid @RequestBody RefundPolicyRequest request) {
        return refundPolicyService.update(id, request);
    }

    @GetMapping("/refund-policies")
    public List<RefundPolicyResponse> policies() {
        return refundPolicyService.findAll();
    }
}
