package com.amritesh.moviebooking.repository;

import com.amritesh.moviebooking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByScreenId(Long screenId);

    List<Show> findByMovieId(Long movieId);

    List<Show> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);
}
