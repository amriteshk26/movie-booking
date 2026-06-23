package com.amritesh.moviebooking.entity;

import com.amritesh.moviebooking.entity.enums.ShowSeatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A bookable seat for a specific show. This is the unit of concurrency control:
 * optimistic locking via {@link Version} prevents double-allocation when
 * multiple users target the same seat simultaneously.
 */
@Entity
@Table(name = "show_seats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "seat_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShowSeatStatus status = ShowSeatStatus.AVAILABLE;

    /** Final computed price for this seat in this show. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Hold currently occupying this seat (null when AVAILABLE or BOOKED). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_hold_id")
    private SeatHold seatHold;

    /** Booking that owns this seat once BOOKED. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Version
    private Long version;
}
