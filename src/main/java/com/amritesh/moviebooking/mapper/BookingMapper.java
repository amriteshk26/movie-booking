package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.BookingResponse;
import com.amritesh.moviebooking.dto.response.ShowSeatResponse;
import com.amritesh.moviebooking.entity.Booking;
import com.amritesh.moviebooking.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingMapper {

    private final ShowSeatMapper showSeatMapper;
    private final PaymentMapper paymentMapper;

    public BookingMapper(ShowSeatMapper showSeatMapper, PaymentMapper paymentMapper) {
        this.showSeatMapper = showSeatMapper;
        this.paymentMapper = paymentMapper;
    }

    public BookingResponse toResponse(Booking booking, Payment payment) {
        List<ShowSeatResponse> seats = booking.getShowSeats().stream()
                .map(showSeatMapper::toResponse)
                .toList();

        return new BookingResponse(
                booking.getId(),
                booking.getShow().getId(),
                booking.getShow().getMovie().getTitle(),
                booking.getShow().getStartTime(),
                booking.getStatus(),
                seats,
                booking.getSubtotal(),
                booking.getDiscountAmount(),
                booking.getTotalAmount(),
                booking.getDiscountCode() != null ? booking.getDiscountCode().getCode() : null,
                paymentMapper.toResponse(payment),
                booking.getCreatedAt(),
                booking.getCancelledAt()
        );
    }
}
