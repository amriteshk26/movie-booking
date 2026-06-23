package com.amritesh.moviebooking;

import com.amritesh.moviebooking.dto.request.*;
import com.amritesh.moviebooking.entity.ShowSeat;
import com.amritesh.moviebooking.entity.User;
import com.amritesh.moviebooking.entity.enums.SeatType;
import com.amritesh.moviebooking.entity.enums.ShowSeatStatus;
import com.amritesh.moviebooking.entity.enums.TierType;
import com.amritesh.moviebooking.repository.PricingTierRepository;
import com.amritesh.moviebooking.repository.ShowSeatRepository;
import com.amritesh.moviebooking.repository.UserRepository;
import com.amritesh.moviebooking.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the system serializes concurrent bookings of the same seat: when two
 * users race for one seat, exactly one hold succeeds and the other is rejected
 * (optimistic locking on ShowSeat). No double-allocation occurs.
 */
@SpringBootTest
@TestPropertySource(properties = "moviebooking.test.context=concurrent")
class ConcurrentSeatHoldIntegrationTest {

    @Autowired SeatHoldService seatHoldService;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired CityService cityService;
    @Autowired TheaterService theaterService;
    @Autowired ScreenService screenService;
    @Autowired SeatService seatService;
    @Autowired MovieService movieService;
    @Autowired ShowService showService;
    @Autowired PricingTierRepository pricingTierRepository;
    @Autowired ShowSeatRepository showSeatRepository;

    private Long showId;
    private Long targetSeatId;
    private Long user1Id;
    private Long user2Id;

    @BeforeEach
    void setUp() {
        user1Id = createCustomer("racer1");
        user2Id = createCustomer("racer2");

        Long tierId = pricingTierRepository.findByTierType(TierType.REGULAR).orElseThrow().getId();
        Long movieId = movieService.create(new MovieRequest("Race Movie", "EN", 100, "U")).id();
        Long cityId = cityService.create(new CityRequest("RaceCity", "RC")).id();
        Long theaterId = theaterService.create(new TheaterRequest("RT", "addr", cityId)).id();
        Long screenId = screenService.create(new ScreenRequest("RS", theaterId)).id();
        seatService.createLayout(new SeatLayoutRequest(screenId,
                List.of(new SeatRowSpec("A", 3, SeatType.REGULAR))));

        LocalDateTime start = LocalDateTime.now().plusDays(2);
        showId = showService.create(new ShowRequest(movieId, screenId, start, start.plusHours(2), tierId, null)).id();
        targetSeatId = showSeatRepository.findByShowId(showId).get(0).getId();
    }

    private Long createCustomer(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPasswordHash(passwordEncoder.encode("pass123"));
        user.setRoles(Set.of(com.amritesh.moviebooking.entity.enums.Role.CUSTOMER));
        return userRepository.save(user).getId();
    }

    @Test
    void twoUsersRacingForOneSeat_onlyOneWins() throws Exception {
        HoldRequest request = new HoldRequest(showId, List.of(targetSeatId));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();

        Callable<Void> attempt = wrapAttempt(request, startGate, successes, failures);

        Future<Void> f1 = pool.submit(attempt);
        Future<Void> f2 = pool.submit(attempt);

        startGate.countDown(); // release both threads at once
        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        pool.shutdown();

        // Exactly one hold succeeds; the other is rejected.
        assertThat(successes.get()).isEqualTo(1);
        assertThat(failures.get()).isEqualTo(1);

        // The seat ended up HELD exactly once (no double-allocation).
        ShowSeat seat = showSeatRepository.findById(targetSeatId).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(ShowSeatStatus.HELD);
        assertThat(seat.getSeatHold()).isNotNull();
    }

    private Callable<Void> wrapAttempt(HoldRequest request, CountDownLatch startGate,
                                       AtomicInteger successes, AtomicInteger failures) {
        // Alternate the user per invocation so the two racers are distinct.
        AtomicInteger turn = new AtomicInteger();
        return () -> {
            Long userId = turn.getAndIncrement() == 0 ? user1Id : user2Id;
            startGate.await();
            try {
                seatHoldService.createHold(userId, request);
                successes.incrementAndGet();
            } catch (Exception ex) {
                failures.incrementAndGet();
            }
            return null;
        };
    }
}
