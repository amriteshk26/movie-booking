package com.amritesh.moviebooking;

import com.amritesh.moviebooking.config.DataSeeder;
import com.amritesh.moviebooking.entity.ShowSeat;
import com.amritesh.moviebooking.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the seeder produces the expected demo dataset and is idempotent.
 * A dedicated context (distinct property) with its own isolated database makes
 * the global counts deterministic regardless of test execution order.
 */
@SpringBootTest
@TestPropertySource(properties = "moviebooking.test.context=data-seeder")
class DataSeederIntegrationTest {

    @Autowired DataSeeder dataSeeder;
    @Autowired UserRepository userRepository;
    @Autowired CityRepository cityRepository;
    @Autowired TheaterRepository theaterRepository;
    @Autowired ShowRepository showRepository;
    @Autowired ShowSeatRepository showSeatRepository;
    @Autowired PricingTierRepository pricingTierRepository;
    @Autowired DiscountCodeRepository discountCodeRepository;
    @Autowired RefundPolicyRepository refundPolicyRepository;

    @Test
    void seedsExpectedCounts() {
        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(cityRepository.count()).isEqualTo(2);
        assertThat(theaterRepository.count()).isEqualTo(3);
        assertThat(showRepository.count()).isEqualTo(4);
        assertThat(pricingTierRepository.count()).isEqualTo(3);
        assertThat(discountCodeRepository.count()).isEqualTo(2);
        assertThat(refundPolicyRepository.count()).isEqualTo(2);
    }

    @Test
    void hasExactlyOneDefaultRefundPolicy() {
        assertThat(refundPolicyRepository.findByIsDefaultTrue()).isPresent();
    }

    @Test
    void showSeatsAreGeneratedAndPriced() {
        Long anyShowId = showRepository.findAll().get(0).getId();
        List<ShowSeat> seats = showSeatRepository.findByShowId(anyShowId);
        assertThat(seats).isNotEmpty();
        assertThat(seats).allSatisfy(s ->
                assertThat(s.getPrice()).isGreaterThan(BigDecimal.ZERO));
    }

    @Test
    void seedingIsIdempotent() {
        long usersBefore = userRepository.count();
        dataSeeder.run();
        assertThat(userRepository.count()).isEqualTo(usersBefore);
    }
}
