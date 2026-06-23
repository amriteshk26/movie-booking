package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.request.TheaterRequest;
import com.amritesh.moviebooking.dto.response.TheaterResponse;
import com.amritesh.moviebooking.entity.City;
import com.amritesh.moviebooking.entity.Theater;
import com.amritesh.moviebooking.exception.ResourceNotFoundException;
import com.amritesh.moviebooking.mapper.TheaterMapper;
import com.amritesh.moviebooking.repository.CityRepository;
import com.amritesh.moviebooking.repository.TheaterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final CityRepository cityRepository;
    private final TheaterMapper theaterMapper;

    public TheaterService(TheaterRepository theaterRepository,
                          CityRepository cityRepository,
                          TheaterMapper theaterMapper) {
        this.theaterRepository = theaterRepository;
        this.cityRepository = cityRepository;
        this.theaterMapper = theaterMapper;
    }

    @Transactional
    public TheaterResponse create(TheaterRequest request) {
        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> ResourceNotFoundException.of("City", request.cityId()));
        Theater theater = new Theater();
        theater.setName(request.name());
        theater.setAddress(request.address());
        theater.setCity(city);
        return theaterMapper.toResponse(theaterRepository.save(theater));
    }

    @Transactional(readOnly = true)
    public List<TheaterResponse> findAll() {
        return theaterRepository.findAll().stream().map(theaterMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TheaterResponse> findByCity(Long cityId) {
        return theaterRepository.findByCityId(cityId).stream().map(theaterMapper::toResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Theater", id));
        theaterRepository.delete(theater);
    }
}
