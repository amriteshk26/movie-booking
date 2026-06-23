package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.request.ShowRequest;
import com.amritesh.moviebooking.dto.response.ShowResponse;
import com.amritesh.moviebooking.dto.response.ShowSeatResponse;
import com.amritesh.moviebooking.entity.*;
import com.amritesh.moviebooking.entity.enums.ShowSeatStatus;
import com.amritesh.moviebooking.exception.BadRequestException;
import com.amritesh.moviebooking.exception.ResourceNotFoundException;
import com.amritesh.moviebooking.mapper.ShowMapper;
import com.amritesh.moviebooking.mapper.ShowSeatMapper;
import com.amritesh.moviebooking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Creates shows and, on creation, materializes a {@link ShowSeat} for every
 * physical seat in the screen with its computed price. Also serves browse and
 * seat-map queries.
 */
@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;
    private final PricingTierRepository pricingTierRepository;
    private final RefundPolicyRepository refundPolicyRepository;
    private final PricingService pricingService;
    private final ShowMapper showMapper;
    private final ShowSeatMapper showSeatMapper;

    public ShowService(ShowRepository showRepository,
                       MovieRepository movieRepository,
                       ScreenRepository screenRepository,
                       SeatRepository seatRepository,
                       ShowSeatRepository showSeatRepository,
                       PricingTierRepository pricingTierRepository,
                       RefundPolicyRepository refundPolicyRepository,
                       PricingService pricingService,
                       ShowMapper showMapper,
                       ShowSeatMapper showSeatMapper) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.showSeatRepository = showSeatRepository;
        this.pricingTierRepository = pricingTierRepository;
        this.refundPolicyRepository = refundPolicyRepository;
        this.pricingService = pricingService;
        this.showMapper = showMapper;
        this.showSeatMapper = showSeatMapper;
    }

    @Transactional
    public ShowResponse create(ShowRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("Show endTime must be after startTime");
        }
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> ResourceNotFoundException.of("Movie", request.movieId()));
        Screen screen = screenRepository.findById(request.screenId())
                .orElseThrow(() -> ResourceNotFoundException.of("Screen", request.screenId()));
        PricingTier tier = pricingTierRepository.findById(request.pricingTierId())
                .orElseThrow(() -> ResourceNotFoundException.of("PricingTier", request.pricingTierId()));

        RefundPolicy refundPolicy = null;
        if (request.refundPolicyId() != null) {
            refundPolicy = refundPolicyRepository.findById(request.refundPolicyId())
                    .orElseThrow(() -> ResourceNotFoundException.of("RefundPolicy", request.refundPolicyId()));
        }

        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setStartTime(request.startTime());
        show.setEndTime(request.endTime());
        show.setPricingTier(tier);
        show.setRefundPolicy(refundPolicy);
        show = showRepository.save(show);

        List<Seat> seats = seatRepository.findByScreenId(screen.getId());
        if (seats.isEmpty()) {
            throw new BadRequestException("Screen has no seat layout; create seats before scheduling a show");
        }
        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : seats) {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShow(show);
            showSeat.setSeat(seat);
            showSeat.setStatus(ShowSeatStatus.AVAILABLE);
            showSeat.setPrice(pricingService.priceFor(tier, seat.getSeatType()));
            showSeats.add(showSeat);
        }
        showSeatRepository.saveAll(showSeats);

        return showMapper.toResponse(show);
    }

    @Transactional(readOnly = true)
    public List<ShowResponse> findAll() {
        return showRepository.findAll().stream().map(showMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ShowResponse findById(Long id) {
        return showMapper.toResponse(getShow(id));
    }

    /** Returns the seat map for a show, ordered by row then seat number. */
    @Transactional(readOnly = true)
    public List<ShowSeatResponse> getSeatMap(Long showId) {
        getShow(showId); // validates existence
        return showSeatRepository.findByShowId(showId).stream()
                .sorted(Comparator.comparing((ShowSeat ss) -> ss.getSeat().getRowLabel())
                        .thenComparing(ss -> ss.getSeat().getSeatNumber()))
                .map(showSeatMapper::toResponse)
                .toList();
    }

    private Show getShow(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Show", id));
    }
}
