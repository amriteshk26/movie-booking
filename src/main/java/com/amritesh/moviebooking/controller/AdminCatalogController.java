package com.amritesh.moviebooking.controller;

import com.amritesh.moviebooking.dto.request.*;
import com.amritesh.moviebooking.dto.response.*;
import com.amritesh.moviebooking.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin management of the location hierarchy (cities, theaters, screens, seat
 * layouts) and content (movies, shows). Restricted to ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogController {

    private final CityService cityService;
    private final TheaterService theaterService;
    private final ScreenService screenService;
    private final SeatService seatService;
    private final MovieService movieService;
    private final ShowService showService;

    public AdminCatalogController(CityService cityService,
                                 TheaterService theaterService,
                                 ScreenService screenService,
                                 SeatService seatService,
                                 MovieService movieService,
                                 ShowService showService) {
        this.cityService = cityService;
        this.theaterService = theaterService;
        this.screenService = screenService;
        this.seatService = seatService;
        this.movieService = movieService;
        this.showService = showService;
    }

    // ---------------- Cities ----------------

    @PostMapping("/cities")
    public ResponseEntity<CityResponse> createCity(@Valid @RequestBody CityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cityService.create(request));
    }

    @PutMapping("/cities/{id}")
    public CityResponse updateCity(@PathVariable Long id, @Valid @RequestBody CityRequest request) {
        return cityService.update(id, request);
    }

    @DeleteMapping("/cities/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------- Theaters ----------------

    @PostMapping("/theaters")
    public ResponseEntity<TheaterResponse> createTheater(@Valid @RequestBody TheaterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.create(request));
    }

    @DeleteMapping("/theaters/{id}")
    public ResponseEntity<Void> deleteTheater(@PathVariable Long id) {
        theaterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------- Screens ----------------

    @PostMapping("/screens")
    public ResponseEntity<ScreenResponse> createScreen(@Valid @RequestBody ScreenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(screenService.create(request));
    }

    // ---------------- Seat layout ----------------

    @PostMapping("/seats/layout")
    public ResponseEntity<List<SeatResponse>> createLayout(@Valid @RequestBody SeatLayoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatService.createLayout(request));
    }

    // ---------------- Movies ----------------

    @PostMapping("/movies")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(request));
    }

    // ---------------- Shows ----------------

    @PostMapping("/shows")
    public ResponseEntity<ShowResponse> createShow(@Valid @RequestBody ShowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.create(request));
    }
}
