package com.amritesh.moviebooking.entity;

import com.amritesh.moviebooking.entity.enums.HoldStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A time-bound reservation of one or more show seats for a user. Expires
 * automatically; a scheduled sweeper releases the seats once {@link #expiresAt}
 * passes without confirmation.
 */
@Entity
@Table(name = "seat_holds")
@Getter
@Setter
@NoArgsConstructor
public class SeatHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @OneToMany(mappedBy = "seatHold", fetch = FetchType.LAZY)
    private List<ShowSeat> showSeats = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HoldStatus status = HoldStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
