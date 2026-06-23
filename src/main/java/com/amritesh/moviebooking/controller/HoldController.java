package com.amritesh.moviebooking.controller;

import com.amritesh.moviebooking.dto.request.HoldRequest;
import com.amritesh.moviebooking.dto.response.HoldResponse;
import com.amritesh.moviebooking.security.SecurityUtils;
import com.amritesh.moviebooking.service.SeatHoldService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Customer seat-hold endpoints. Holds are time-bound and released automatically
 * on expiry.
 */
@RestController
@RequestMapping("/api/holds")
@PreAuthorize("hasRole('CUSTOMER')")
public class HoldController {

    private final SeatHoldService seatHoldService;
    private final SecurityUtils securityUtils;

    public HoldController(SeatHoldService seatHoldService, SecurityUtils securityUtils) {
        this.seatHoldService = seatHoldService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<HoldResponse> createHold(@Valid @RequestBody HoldRequest request) {
        HoldResponse response = seatHoldService.createHold(securityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{holdId}")
    public ResponseEntity<Void> releaseHold(@PathVariable Long holdId) {
        seatHoldService.releaseHold(securityUtils.getCurrentUserId(), holdId);
        return ResponseEntity.noContent().build();
    }
}
