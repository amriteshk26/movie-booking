package com.amritesh.moviebooking.config;

import com.amritesh.moviebooking.dto.request.*;
import com.amritesh.moviebooking.dto.response.*;
import com.amritesh.moviebooking.entity.User;
import com.amritesh.moviebooking.entity.enums.DiscountType;
import com.amritesh.moviebooking.entity.enums.Role;
import com.amritesh.moviebooking.entity.enums.SeatType;
import com.amritesh.moviebooking.entity.enums.TierType;
import com.amritesh.moviebooking.repository.UserRepository;
import com.amritesh.moviebooking.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Seeds a demo-sized dataset on first startup so every flow is usable
 * immediately. Idempotent: skips if users already exist.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CityService cityService;
    private final TheaterService theaterService;
    private final ScreenService screenService;
    private final SeatService seatService;
    private final MovieService movieService;
    private final ShowService showService;
    private final PricingTierService pricingTierService;
    private final RefundPolicyService refundPolicyService;
    private final DiscountService discountService;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      CityService cityService,
                      TheaterService theaterService,
                      ScreenService screenService,
                      SeatService seatService,
                      MovieService movieService,
                      ShowService showService,
                      PricingTierService pricingTierService,
                      RefundPolicyService refundPolicyService,
                      DiscountService discountService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cityService = cityService;
        this.theaterService = theaterService;
        this.screenService = screenService;
        this.seatService = seatService;
        this.movieService = movieService;
        this.showService = showService;
        this.pricingTierService = pricingTierService;
        this.refundPolicyService = refundPolicyService;
        this.discountService = discountService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Seed skipped: data already present");
            return;
        }

        seedUsers();
        seedPricingTiers();
        seedRefundPolicies();
        seedDiscountCodes();
        seedCatalog();

        log.info("=========================================================");
        log.info(" Demo data seeded. Login (HTTP Basic):");
        log.info("   admin    / admin123     (ROLE_ADMIN)");
        log.info("   customer / customer123  (ROLE_CUSTOMER)");
        log.info("   alice    / alice123     (ROLE_CUSTOMER)");
        log.info(" Discount codes: WELCOME10 (10%), FLAT50 (flat 50, max 3 uses)");
        log.info("=========================================================");
    }

    private void seedUsers() {
        createUser("admin", "admin@moviebooking.test", "admin123", Role.ADMIN);
        createUser("customer", "customer@moviebooking.test", "customer123", Role.CUSTOMER);
        createUser("alice", "alice@moviebooking.test", "alice123", Role.CUSTOMER);
    }

    private void createUser(String username, String email, String rawPassword, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }

    private void seedPricingTiers() {
        pricingTierService.create(new PricingTierRequest(
                TierType.REGULAR, new BigDecimal("200.00"), new BigDecimal("1.50")));
        pricingTierService.create(new PricingTierRequest(
                TierType.PREMIUM, new BigDecimal("350.00"), new BigDecimal("1.50")));
        pricingTierService.create(new PricingTierRequest(
                TierType.WEEKEND, new BigDecimal("300.00"), new BigDecimal("1.60")));
    }

    private void seedRefundPolicies() {
        // Default policy applied when a show has none set.
        refundPolicyService.create(new RefundPolicyRequest(
                "Standard", 48, 24, 100, 50, 0, true));
        refundPolicyService.create(new RefundPolicyRequest(
                "Flexible", 24, 6, 100, 75, 25, false));
    }

    private void seedDiscountCodes() {
        discountService.create(new DiscountCodeRequest(
                "WELCOME10", DiscountType.PERCENT, new BigDecimal("10"),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusMonths(1), null));
        discountService.create(new DiscountCodeRequest(
                "FLAT50", DiscountType.FLAT, new BigDecimal("50"),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusMonths(1), 3));
    }

    private void seedCatalog() {
        Long tierRegular = pricingTierService.findAll().stream()
                .filter(t -> t.tierType() == TierType.REGULAR).findFirst().orElseThrow().id();
        Long tierWeekend = pricingTierService.findAll().stream()
                .filter(t -> t.tierType() == TierType.WEEKEND).findFirst().orElseThrow().id();

        // ---- Movies ----
        Long inception = movieService.create(new MovieRequest(
                "Inception", "English", 148, "UA")).id();
        Long dangal = movieService.create(new MovieRequest(
                "Dangal", "Hindi", 161, "U")).id();
        Long interstellar = movieService.create(new MovieRequest(
                "Interstellar", "English", 169, "UA")).id();

        // ---- City 1: Bengaluru ----
        Long bengaluru = cityService.create(new CityRequest("Bengaluru", "Karnataka")).id();
        Long pvrForum = theaterService.create(new TheaterRequest(
                "PVR Forum Mall", "Koramangala", bengaluru)).id();
        Long inoxGaruda = theaterService.create(new TheaterRequest(
                "INOX Garuda Mall", "Magrath Road", bengaluru)).id();

        Long pvrScreen1 = screenService.create(new ScreenRequest("Audi 1", pvrForum)).id();
        Long pvrScreen2 = screenService.create(new ScreenRequest("Audi 2", pvrForum)).id();
        Long inoxScreen1 = screenService.create(new ScreenRequest("Screen 1", inoxGaruda)).id();

        seedLayout(pvrScreen1);
        seedLayout(pvrScreen2);
        seedLayout(inoxScreen1);

        // ---- City 2: Mumbai ----
        Long mumbai = cityService.create(new CityRequest("Mumbai", "Maharashtra")).id();
        Long pvrPhoenix = theaterService.create(new TheaterRequest(
                "PVR Phoenix", "Lower Parel", mumbai)).id();
        Long mumbaiScreen1 = screenService.create(new ScreenRequest("IMAX", pvrPhoenix)).id();
        seedLayout(mumbaiScreen1);

        // ---- Shows (future times; one within the reminder window) ----
        LocalDateTime now = LocalDateTime.now();
        createShow(inception, pvrScreen1, now.plusDays(1).withHour(18).withMinute(0), tierRegular, null);
        createShow(dangal, pvrScreen2, now.plusDays(1).withHour(21).withMinute(0), tierRegular, null);
        createShow(interstellar, inoxScreen1, now.plusDays(2).withHour(20).withMinute(0), tierWeekend, null);
        // Soon-starting show to demonstrate the reminder window.
        createShow(inception, mumbaiScreen1, now.plusMinutes(90), tierRegular, null);
    }

    private void seedLayout(Long screenId) {
        List<SeatRowSpec> rows = List.of(
                new SeatRowSpec("A", 10, SeatType.PREMIUM),
                new SeatRowSpec("B", 10, SeatType.PREMIUM),
                new SeatRowSpec("C", 12, SeatType.REGULAR),
                new SeatRowSpec("D", 12, SeatType.REGULAR),
                new SeatRowSpec("E", 12, SeatType.REGULAR)
        );
        seatService.createLayout(new SeatLayoutRequest(screenId, rows));
    }

    private void createShow(Long movieId, Long screenId, LocalDateTime start,
                            Long pricingTierId, Long refundPolicyId) {
        showService.create(new ShowRequest(
                movieId, screenId, start, start.plusHours(3), pricingTierId, refundPolicyId));
    }
}
