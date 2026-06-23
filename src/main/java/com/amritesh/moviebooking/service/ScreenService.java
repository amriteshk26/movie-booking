package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.request.ScreenRequest;
import com.amritesh.moviebooking.dto.response.ScreenResponse;
import com.amritesh.moviebooking.entity.Screen;
import com.amritesh.moviebooking.entity.Theater;
import com.amritesh.moviebooking.exception.ResourceNotFoundException;
import com.amritesh.moviebooking.mapper.ScreenMapper;
import com.amritesh.moviebooking.repository.ScreenRepository;
import com.amritesh.moviebooking.repository.TheaterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheaterRepository theaterRepository;
    private final ScreenMapper screenMapper;

    public ScreenService(ScreenRepository screenRepository,
                         TheaterRepository theaterRepository,
                         ScreenMapper screenMapper) {
        this.screenRepository = screenRepository;
        this.theaterRepository = theaterRepository;
        this.screenMapper = screenMapper;
    }

    @Transactional
    public ScreenResponse create(ScreenRequest request) {
        Theater theater = theaterRepository.findById(request.theaterId())
                .orElseThrow(() -> ResourceNotFoundException.of("Theater", request.theaterId()));
        Screen screen = new Screen();
        screen.setName(request.name());
        screen.setTheater(theater);
        return screenMapper.toResponse(screenRepository.save(screen));
    }

    @Transactional(readOnly = true)
    public List<ScreenResponse> findByTheater(Long theaterId) {
        return screenRepository.findByTheaterId(theaterId).stream().map(screenMapper::toResponse).toList();
    }
}
