package com.amritesh.moviebooking.controller;

import com.amritesh.moviebooking.dto.request.ConfirmBookingRequest;
import com.amritesh.moviebooking.dto.response.BookingResponse;
import com.amritesh.moviebooking.dto.response.CancellationResponse;
import com.amritesh.moviebooking.security.SecurityUtils;
import com.amritesh.moviebooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer booking endpoints: confirm a hold into a booking, view history,
 * and cancel (triggering a policy-driven refund).
 */
@RestController
@RequestMapping("/api/bookings")
@PreAuthorize("hasRole('CUSTOMER')")
public class BookingController {

    private final BookingService bookingService;
    private final SecurityUtils securityUtils;

    public BookingController(BookingService bookingService, SecurityUtils securityUtils) {
        this.bookingService = bookingService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/confirm")
    public ResponseEntity<BookingResponse> confirm(@Valid @RequestBody ConfirmBookingRequest request) {
        BookingResponse response = bookingService.confirm(securityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<BookingResponse> history() {
        return bookingService.history(securityUtils.getCurrentUserId());
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable Long bookingId) {
        return bookingService.getBooking(securityUtils.getCurrentUserId(), bookingId);
    }

    @PostMapping("/{bookingId}/cancel")
    public CancellationResponse cancel(@PathVariable Long bookingId) {
        return bookingService.cancel(securityUtils.getCurrentUserId(), bookingId);
    }
}
