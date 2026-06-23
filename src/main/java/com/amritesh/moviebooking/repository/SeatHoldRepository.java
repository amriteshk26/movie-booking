package com.amritesh.moviebooking.repository;

import com.amritesh.moviebooking.entity.SeatHold;
import com.amritesh.moviebooking.entity.enums.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    List<SeatHold> findByStatusAndExpiresAtBefore(HoldStatus status, LocalDateTime cutoff);

    List<SeatHold> findByUserId(Long userId);
}
