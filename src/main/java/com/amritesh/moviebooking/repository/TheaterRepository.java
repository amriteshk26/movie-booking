package com.amritesh.moviebooking.repository;

import com.amritesh.moviebooking.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheaterRepository extends JpaRepository<Theater, Long> {

    List<Theater> findByCityId(Long cityId);
}
