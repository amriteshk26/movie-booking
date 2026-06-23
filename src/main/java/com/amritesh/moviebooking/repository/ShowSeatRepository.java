package com.amritesh.moviebooking.repository;

import com.amritesh.moviebooking.entity.ShowSeat;
import com.amritesh.moviebooking.entity.enums.ShowSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowId(Long showId);

    List<ShowSeat> findByShowIdAndStatus(Long showId, ShowSeatStatus status);

    List<ShowSeat> findByIdInAndShowId(List<Long> ids, Long showId);

    List<ShowSeat> findBySeatHoldId(Long seatHoldId);
}
