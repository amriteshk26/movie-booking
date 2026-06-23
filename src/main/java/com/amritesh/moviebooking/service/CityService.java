package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.request.CityRequest;
import com.amritesh.moviebooking.dto.response.CityResponse;
import com.amritesh.moviebooking.entity.City;
import com.amritesh.moviebooking.exception.ResourceNotFoundException;
import com.amritesh.moviebooking.mapper.CityMapper;
import com.amritesh.moviebooking.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    public CityService(CityRepository cityRepository, CityMapper cityMapper) {
        this.cityRepository = cityRepository;
        this.cityMapper = cityMapper;
    }

    @Transactional
    public CityResponse create(CityRequest request) {
        City city = new City();
        city.setName(request.name());
        city.setState(request.state());
        return cityMapper.toResponse(cityRepository.save(city));
    }

    @Transactional(readOnly = true)
    public List<CityResponse> findAll() {
        return cityRepository.findAll().stream().map(cityMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CityResponse findById(Long id) {
        return cityMapper.toResponse(getCity(id));
    }

    @Transactional
    public CityResponse update(Long id, CityRequest request) {
        City city = getCity(id);
        city.setName(request.name());
        city.setState(request.state());
        return cityMapper.toResponse(city);
    }

    @Transactional
    public void delete(Long id) {
        cityRepository.delete(getCity(id));
    }

    private City getCity(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("City", id));
    }
}
