package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.request.ConfirmBookingRequest;
import com.amritesh.moviebooking.dto.response.BookingResponse;
import com.amritesh.moviebooking.dto.response.CancellationResponse;
import com.amritesh.moviebooking.entity.*;
import com.amritesh.moviebooking.entity.enums.BookingStatus;
import com.amritesh.moviebooking.entity.enums.HoldStatus;
import com.amritesh.moviebooking.entity.enums.NotificationType;
import com.amritesh.moviebooking.entity.enums.ShowSeatStatus;
import com.amritesh.moviebooking.exception.BadRequestException;
import com.amritesh.moviebooking.exception.ForbiddenException;
import com.amritesh.moviebooking.exception.ResourceNotFoundException;
import com.amritesh.moviebooking.mapper.BookingMapper;
import com.amritesh.moviebooking.repository.BookingRepository;
import com.amritesh.moviebooking.repository.SeatHoldRepository;
import com.amritesh.moviebooking.repository.ShowSeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Confirms holds into bookings (with mock payment and optional discount) and
 * handles cancellations with policy-driven refunds. Confirmation and
 * cancellation fire asynchronous notifications so the flow is never blocked.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final ShowSeatRepository showSeatRepository;
    private final PaymentService paymentService;
    private final DiscountService discountService;
    private final RefundService refundService;
    private final NotificationService notificationService;
    private final BookingMapper bookingMapper;

    public BookingService(BookingRepository bookingRepository,
                          SeatHoldRepository seatHoldRepository,
                          ShowSeatRepository showSeatRepository,
                          PaymentService paymentService,
                          DiscountService discountService,
                          RefundService refundService,
                          NotificationService notificationService,
                          BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.showSeatRepository = showSeatRepository;
        this.paymentService = paymentService;
        this.discountService = discountService;
        this.refundService = refundService;
        this.notificationService = notificationService;
        this.bookingMapper = bookingMapper;
    }

    @Transactional
    public BookingResponse confirm(Long userId, ConfirmBookingRequest request) {
        SeatHold hold = seatHoldRepository.findById(request.holdId())
                .orElseThrow(() -> ResourceNotFoundException.of("SeatHold", request.holdId()));

        if (!hold.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this hold");
        }
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw new BadRequestException("Hold is not active (status=" + hold.getStatus() + ")");
        }
        if (LocalDateTime.now().isAfter(hold.getExpiresAt())) {
            // Expired but not yet swept: release now and reject.
            releaseHeldSeats(hold);
            hold.setStatus(HoldStatus.EXPIRED);
            throw new BadRequestException("Hold has expired; please re-select your seats");
        }

        List<ShowSeat> seats = showSeatRepository.findBySeatHoldId(hold.getId()).stream()
                .filter(s -> s.getStatus() == ShowSeatStatus.HELD)
                .toList();
        if (seats.isEmpty()) {
            throw new BadRequestException("No held seats found for this hold");
        }

        BigDecimal subtotal = seats.stream()
                .map(ShowSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DiscountService.DiscountApplication discount = discountService.apply(request.discountCode(), subtotal);
        BigDecimal total = subtotal.subtract(discount.amount());

        Booking booking = new Booking();
        booking.setUser(hold.getUser());
        booking.setShow(hold.getShow());
        booking.setStatus(BookingStatus.PENDING);
        booking.setSubtotal(subtotal);
        booking.setDiscountAmount(discount.amount());
        booking.setTotalAmount(total);
        booking.setDiscountCode(discount.code());
        booking.setCreatedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        // Mock payment.
        Payment payment = paymentService.charge(booking, total);

        // Commit seats to the booking.
        for (ShowSeat seat : seats) {
            seat.setStatus(ShowSeatStatus.BOOKED);
            seat.setBooking(booking);
            seat.setSeatHold(null);
        }
        showSeatRepository.saveAll(seats);

        booking.setStatus(BookingStatus.CONFIRMED);
        hold.setStatus(HoldStatus.CONVERTED);

        // Async confirmation (non-blocking).
        String message = String.format(
                "Booking #%d confirmed for '%s' at %s. %d seat(s), total %s.",
                booking.getId(), hold.getShow().getMovie().getTitle(),
                hold.getShow().getStartTime(), seats.size(), total);
        notificationService.send(userId, NotificationType.BOOKING_CONFIRMATION, message);

        return bookingMapper.toResponse(booking, payment);
    }

    @Transactional
    public CancellationResponse cancel(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));
        if (!booking.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this booking");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only confirmed bookings can be cancelled (status="
                    + booking.getStatus() + ")");
        }

        RefundService.RefundResult refund = refundService.computeRefund(booking.getShow(), booking.getTotalAmount());

        Payment payment = paymentService.findByBookingId(bookingId);
        if (payment != null) {
            paymentService.refund(payment, refund.amount());
        }

        // Release seats back to the pool.
        List<ShowSeat> seats = booking.getShowSeats();
        for (ShowSeat seat : seats) {
            seat.setStatus(ShowSeatStatus.AVAILABLE);
            seat.setBooking(null);
        }
        showSeatRepository.saveAll(seats);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        String message = String.format(
                "Booking #%d cancelled. Refund: %s (%d%%).",
                booking.getId(), refund.amount(), refund.percent());
        notificationService.send(userId, NotificationType.BOOKING_CANCELLATION, message);

        return new CancellationResponse(
                booking.getId(),
                booking.getStatus(),
                refund.percent(),
                refund.amount(),
                message
        );
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> history(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(b -> bookingMapper.toResponse(b, paymentService.findByBookingId(b.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));
        if (!booking.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this booking");
        }
        return bookingMapper.toResponse(booking, paymentService.findByBookingId(bookingId));
    }

    private void releaseHeldSeats(SeatHold hold) {
        List<ShowSeat> seats = showSeatRepository.findBySeatHoldId(hold.getId());
        for (ShowSeat seat : seats) {
            if (seat.getStatus() == ShowSeatStatus.HELD) {
                seat.setStatus(ShowSeatStatus.AVAILABLE);
                seat.setSeatHold(null);
            }
        }
        showSeatRepository.saveAll(seats);
    }
}
