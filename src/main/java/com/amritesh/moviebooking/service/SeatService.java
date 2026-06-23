package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.request.SeatLayoutRequest;
import com.amritesh.moviebooking.dto.request.SeatRowSpec;
import com.amritesh.moviebooking.dto.response.SeatResponse;
import com.amritesh.moviebooking.entity.Screen;
import com.amritesh.moviebooking.entity.Seat;
import com.amritesh.moviebooking.exception.ResourceNotFoundException;
import com.amritesh.moviebooking.mapper.SeatMapper;
import com.amritesh.moviebooking.repository.ScreenRepository;
import com.amritesh.moviebooking.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the physical seat layout of a screen.
 */
@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;
    private final SeatMapper seatMapper;

    public SeatService(SeatRepository seatRepository,
                       ScreenRepository screenRepository,
                       SeatMapper seatMapper) {
        this.seatRepository = seatRepository;
        this.screenRepository = screenRepository;
        this.seatMapper = seatMapper;
    }

    @Transactional
    public List<SeatResponse> createLayout(SeatLayoutRequest request) {
        Screen screen = screenRepository.findById(request.screenId())
                .orElseThrow(() -> ResourceNotFoundException.of("Screen", request.screenId()));

        List<Seat> seats = new ArrayList<>();
        for (SeatRowSpec row : request.rows()) {
            for (int n = 1; n <= row.seatCount(); n++) {
                Seat seat = new Seat();
                seat.setScreen(screen);
                seat.setRowLabel(row.rowLabel());
                seat.setSeatNumber(n);
                seat.setSeatType(row.seatType());
                seats.add(seat);
            }
        }
        return seatRepository.saveAll(seats).stream().map(seatMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> findByScreen(Long screenId) {
        return seatRepository.findByScreenId(screenId).stream().map(seatMapper::toResponse).toList();
    }
}
