package com.amritesh.moviebooking.repository;

import com.amritesh.moviebooking.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {
}
