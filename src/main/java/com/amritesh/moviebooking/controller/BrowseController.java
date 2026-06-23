package com.amritesh.moviebooking.controller;

import com.amritesh.moviebooking.dto.response.*;
import com.amritesh.moviebooking.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only browse endpoints available to any authenticated user (customer or
 * admin): cities, theaters, screens, movies, shows and seat maps.
 */
@RestController
@RequestMapping("/api")
public class BrowseController {

    private final CityService cityService;
    private final TheaterService theaterService;
    private final ScreenService screenService;
    private final SeatService seatService;
    private final MovieService movieService;
    private final ShowService showService;

    public BrowseController(CityService cityService,
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

    @GetMapping("/cities")
    public List<CityResponse> cities() {
        return cityService.findAll();
    }

    @GetMapping("/cities/{cityId}/theaters")
    public List<TheaterResponse> theatersInCity(@PathVariable Long cityId) {
        return theaterService.findByCity(cityId);
    }

    @GetMapping("/theaters/{theaterId}/screens")
    public List<ScreenResponse> screensInTheater(@PathVariable Long theaterId) {
        return screenService.findByTheater(theaterId);
    }

    @GetMapping("/screens/{screenId}/seats")
    public List<SeatResponse> seatsInScreen(@PathVariable Long screenId) {
        return seatService.findByScreen(screenId);
    }

    @GetMapping("/movies")
    public List<MovieResponse> movies() {
        return movieService.findAll();
    }

    @GetMapping("/shows")
    public List<ShowResponse> shows() {
        return showService.findAll();
    }

    @GetMapping("/shows/{showId}")
    public ShowResponse show(@PathVariable Long showId) {
        return showService.findById(showId);
    }

    @GetMapping("/shows/{showId}/seats")
    public List<ShowSeatResponse> seatMap(@PathVariable Long showId) {
        return showService.getSeatMap(showId);
    }
}
