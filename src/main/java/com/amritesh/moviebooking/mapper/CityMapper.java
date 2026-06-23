package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.CityResponse;
import com.amritesh.moviebooking.entity.City;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {

    public CityResponse toResponse(City city) {
        return new CityResponse(city.getId(), city.getName(), city.getState());
    }
}
