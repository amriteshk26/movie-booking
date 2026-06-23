package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.config.BookingProperties;
import com.amritesh.moviebooking.dto.request.HoldRequest;
import com.amritesh.moviebooking.dto.response.HoldResponse;
import com.amritesh.moviebooking.entity.SeatHold;
import com.amritesh.moviebooking.entity.Show;
import com.amritesh.moviebooking.entity.ShowSeat;
import com.amritesh.moviebooking.entity.User;
import com.amritesh.moviebooking.entity.enums.HoldStatus;
import com.amritesh.moviebooking.entity.enums.ShowSeatStatus;
import com.amritesh.moviebooking.exception.ConflictException;
import com.amritesh.moviebooking.exception.ForbiddenException;
import com.amritesh.moviebooking.exception.ResourceNotFoundException;
import com.amritesh.moviebooking.mapper.ShowSeatMapper;
import com.amritesh.moviebooking.repository.SeatHoldRepository;
import com.amritesh.moviebooking.repository.ShowRepository;
import com.amritesh.moviebooking.repository.ShowSeatRepository;
import com.amritesh.moviebooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Places and releases time-bound seat holds. Concurrency safety relies on
 * optimistic locking ({@code @Version}) on {@link ShowSeat}: when two users
 * target the same seat, exactly one hold succeeds and the other receives a 409.
 */
@Service
public class SeatHoldService {

    private static final Logger log = LoggerFactory.getLogger(SeatHoldService.class);

    private final SeatHoldRepository seatHoldRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final ShowSeatMapper showSeatMapper;
    private final BookingProperties bookingProperties;

    public SeatHoldService(SeatHoldRepository seatHoldRepository,
                           ShowSeatRepository showSeatRepository,
                           ShowRepository showRepository,
                           UserRepository userRepository,
                           ShowSeatMapper showSeatMapper,
                           BookingProperties bookingProperties) {
        this.seatHoldRepository = seatHoldRepository;
        this.showSeatRepository = showSeatRepository;
        this.showRepository = showRepository;
        this.userRepository = userRepository;
        this.showSeatMapper = showSeatMapper;
        this.bookingProperties = bookingProperties;
    }

    @Transactional
    public HoldResponse createHold(Long userId, HoldRequest request) {
        Show show = showRepository.findById(request.showId())
                .orElseThrow(() -> ResourceNotFoundException.of("Show", request.showId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        List<Long> requestedIds = request.showSeatIds().stream().distinct().toList();
        List<ShowSeat> seats = showSeatRepository.findByIdInAndShowId(requestedIds, show.getId());
        if (seats.size() != requestedIds.size()) {
            throw new ResourceNotFoundException("One or more seats do not belong to show " + show.getId());
        }

        // Pre-check: all must be currently available.
        for (ShowSeat seat : seats) {
            if (seat.getStatus() != ShowSeatStatus.AVAILABLE) {
                throw new ConflictException("Seat " + seatLabel(seat) + " is no longer available");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        SeatHold hold = new SeatHold();
        hold.setUser(user);
        hold.setShow(show);
        hold.setStatus(HoldStatus.ACTIVE);
        hold.setCreatedAt(now);
        hold.setExpiresAt(now.plusSeconds(bookingProperties.getTtlSeconds()));
        hold = seatHoldRepository.save(hold);

        for (ShowSeat seat : seats) {
            seat.setStatus(ShowSeatStatus.HELD);
            seat.setSeatHold(hold);
        }

        try {
            // Force a flush so a concurrent hold on the same seat triggers the
            // optimistic-lock failure here, where we can translate it to a 409.
            showSeatRepository.saveAllAndFlush(seats);
        } catch (OptimisticLockingFailureException ex) {
            throw new ConflictException("One or more selected seats were just taken by another user");
        }

        BigDecimal total = seats.stream()
                .map(ShowSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new HoldResponse(
                hold.getId(),
                show.getId(),
                hold.getStatus().name(),
                hold.getExpiresAt(),
                seats.stream().map(showSeatMapper::toResponse).toList(),
                total
        );
    }

    @Transactional
    public void releaseHold(Long userId, Long holdId) {
        SeatHold hold = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> ResourceNotFoundException.of("SeatHold", holdId));
        if (!hold.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this hold");
        }
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            return; // already released/converted/expired; nothing to do
        }
        releaseSeats(hold);
        hold.setStatus(HoldStatus.RELEASED);
    }

    /**
     * Periodically releases seats whose holds have expired without confirmation.
     */
    @Scheduled(fixedDelayString = "${booking.hold.sweep-interval-ms}")
    @Transactional
    public void expireStaleHolds() {
        List<SeatHold> expired = seatHoldRepository
                .findByStatusAndExpiresAtBefore(HoldStatus.ACTIVE, LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }
        for (SeatHold hold : expired) {
            releaseSeats(hold);
            hold.setStatus(HoldStatus.EXPIRED);
        }
        log.info("Expired {} stale seat hold(s)", expired.size());
    }

    private void releaseSeats(SeatHold hold) {
        List<ShowSeat> seats = showSeatRepository.findBySeatHoldId(hold.getId());
        for (ShowSeat seat : seats) {
            if (seat.getStatus() == ShowSeatStatus.HELD) {
                seat.setStatus(ShowSeatStatus.AVAILABLE);
                seat.setSeatHold(null);
            }
        }
        showSeatRepository.saveAll(seats);
    }

    private String seatLabel(ShowSeat seat) {
        return seat.getSeat().getRowLabel() + seat.getSeat().getSeatNumber();
    }
}
