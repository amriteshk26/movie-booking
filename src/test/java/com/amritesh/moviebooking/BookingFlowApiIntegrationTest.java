package com.amritesh.moviebooking;

import com.amritesh.moviebooking.dto.request.*;
import com.amritesh.moviebooking.entity.ShowSeat;
import com.amritesh.moviebooking.entity.enums.TierType;
import com.amritesh.moviebooking.repository.PricingTierRepository;
import com.amritesh.moviebooking.repository.ShowSeatRepository;
import com.amritesh.moviebooking.repository.UserRepository;
import com.amritesh.moviebooking.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end API coverage of the core customer flow plus auth and RBAC.
 * The test provisions its own show so it does not depend on seed specifics.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "moviebooking.test.context=booking-flow")
class BookingFlowApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    @Autowired CityService cityService;
    @Autowired TheaterService theaterService;
    @Autowired ScreenService screenService;
    @Autowired SeatService seatService;
    @Autowired MovieService movieService;
    @Autowired ShowService showService;
    @Autowired PricingTierRepository pricingTierRepository;
    @Autowired ShowSeatRepository showSeatRepository;

    private Long showId;
    private List<Long> seatIds;

    private static final String USER = "flowcustomer";
    private static final String PASS = "secret123";

    @BeforeEach
    void setUp() throws Exception {
        // Register the customer once; @BeforeEach runs per test method but the
        // context (and DB) is shared across this class's methods.
        if (!userRepository.existsByUsername(USER)) {
            RegisterRequest reg = new RegisterRequest(USER, USER + "@test.com", PASS);
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated());
        }

        // Provision an isolated show with 5 regular seats (price 200 each).
        Long tierId = pricingTierRepository.findByTierType(TierType.REGULAR).orElseThrow().getId();
        Long movieId = movieService.create(new MovieRequest("Test Movie", "EN", 120, "UA")).id();
        Long cityId = cityService.create(new CityRequest("Testville", "TS")).id();
        Long theaterId = theaterService.create(new TheaterRequest("T", "addr", cityId)).id();
        Long screenId = screenService.create(new ScreenRequest("S1", theaterId)).id();
        seatService.createLayout(new SeatLayoutRequest(screenId,
                List.of(new SeatRowSpec("A", 5, com.amritesh.moviebooking.entity.enums.SeatType.REGULAR))));

        LocalDateTime start = LocalDateTime.now().plusDays(3);
        showId = showService.create(new ShowRequest(movieId, screenId, start, start.plusHours(2), tierId, null)).id();

        // Sort by ShowSeat id (creation order) to avoid touching the lazy Seat
        // association outside a transaction.
        seatIds = showSeatRepository.findByShowId(showId).stream()
                .map(ShowSeat::getId)
                .sorted()
                .toList();
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/me").with(httpBasic(USER, PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USER))
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));
    }

    @Test
    void customerCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(post("/api/admin/cities")
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"state\":\"Y\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validationErrorsReturnFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"email\":\"bad\",\"password\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void fullBookingLifecycle() throws Exception {
        // Browse seat map.
        mockMvc.perform(get("/api/shows/{id}/seats", showId).with(httpBasic(USER, PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));

        // Hold two seats.
        HoldRequest holdReq = new HoldRequest(showId, List.of(seatIds.get(0), seatIds.get(1)));
        MvcResult holdResult = mockMvc.perform(post("/api/holds")
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(400.00))
                .andReturn();
        long holdId = json(holdResult).get("holdId").asLong();

        // Confirm with a 10% discount (WELCOME10 is seeded).
        ConfirmBookingRequest confirmReq = new ConfirmBookingRequest(holdId, "WELCOME10");
        MvcResult bookingResult = mockMvc.perform(post("/api/bookings/confirm")
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.subtotal").value(400.00))
                .andExpect(jsonPath("$.discountAmount").value(40.00))
                .andExpect(jsonPath("$.totalAmount").value(360.00))
                .andExpect(jsonPath("$.payment.status").value("SUCCESS"))
                .andReturn();
        long bookingId = json(bookingResult).get("id").asLong();

        // Seats are now booked.
        mockMvc.perform(get("/api/shows/{id}/seats", showId).with(httpBasic(USER, PASS)))
                .andExpect(jsonPath("$[?(@.status == 'BOOKED')]").isNotEmpty());

        // History contains the booking.
        mockMvc.perform(get("/api/bookings").with(httpBasic(USER, PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value((int) bookingId));

        // Cancel -> full refund (show is 3 days out, default policy gives 100%).
        mockMvc.perform(post("/api/bookings/{id}/cancel", bookingId).with(httpBasic(USER, PASS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refundPercent").value(100))
                .andExpect(jsonPath("$.refundAmount").value(360.00));

        // Seats released back to AVAILABLE.
        List<ShowSeat> after = showSeatRepository.findByShowId(showId);
        assertThat(after).filteredOn(s -> seatIds.contains(s.getId()))
                .allSatisfy(s -> assertThat(s.getStatus().name()).isEqualTo("AVAILABLE"));
    }

    @Test
    void holdingAnAlreadyHeldSeatConflicts() throws Exception {
        HoldRequest holdReq = new HoldRequest(showId, List.of(seatIds.get(2)));
        mockMvc.perform(post("/api/holds")
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdReq)))
                .andExpect(status().isCreated());

        // Second hold on the same seat must conflict.
        mockMvc.perform(post("/api/holds")
                        .with(httpBasic(USER, PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdReq)))
                .andExpect(status().isConflict());
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
