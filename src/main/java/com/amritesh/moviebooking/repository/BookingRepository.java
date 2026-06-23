package com.amritesh.moviebooking.repository;

import com.amritesh.moviebooking.entity.Booking;
import com.amritesh.moviebooking.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByStatusAndReminderSentFalseAndShowStartTimeBetween(
            BookingStatus status, LocalDateTime from, LocalDateTime to);
}
