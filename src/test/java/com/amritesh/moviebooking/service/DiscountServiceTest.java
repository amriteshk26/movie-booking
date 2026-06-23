package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.entity.DiscountCode;
import com.amritesh.moviebooking.entity.enums.DiscountType;
import com.amritesh.moviebooking.exception.BadRequestException;
import com.amritesh.moviebooking.mapper.DiscountCodeMapper;
import com.amritesh.moviebooking.repository.DiscountCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountCodeRepository discountCodeRepository;
    @Mock
    private DiscountCodeMapper discountCodeMapper;
    @InjectMocks
    private DiscountService discountService;

    private DiscountCode code(DiscountType type, String value) {
        DiscountCode code = new DiscountCode();
        code.setCode("CODE");
        code.setDiscountType(type);
        code.setValue(new BigDecimal(value));
        code.setActive(true);
        code.setUsedCount(0);
        return code;
    }

    @Test
    void blankCodeProducesZeroDiscount() {
        var result = discountService.apply(null, new BigDecimal("400.00"));
        assertThat(result.amount()).isEqualByComparingTo("0.00");
        assertThat(result.code()).isNull();
    }

    @Test
    void percentDiscountIsComputed() {
        when(discountCodeRepository.findByCode("CODE")).thenReturn(Optional.of(code(DiscountType.PERCENT, "10")));
        var result = discountService.apply("CODE", new BigDecimal("400.00"));
        assertThat(result.amount()).isEqualByComparingTo("40.00");
        assertThat(result.code().getUsedCount()).isEqualTo(1);
    }

    @Test
    void flatDiscountIsComputed() {
        when(discountCodeRepository.findByCode("CODE")).thenReturn(Optional.of(code(DiscountType.FLAT, "50")));
        var result = discountService.apply("CODE", new BigDecimal("400.00"));
        assertThat(result.amount()).isEqualByComparingTo("50.00");
    }

    @Test
    void discountIsCappedAtSubtotal() {
        when(discountCodeRepository.findByCode("CODE")).thenReturn(Optional.of(code(DiscountType.FLAT, "5000")));
        var result = discountService.apply("CODE", new BigDecimal("400.00"));
        assertThat(result.amount()).isEqualByComparingTo("400.00");
    }

    @Test
    void inactiveCodeIsRejected() {
        DiscountCode code = code(DiscountType.PERCENT, "10");
        code.setActive(false);
        when(discountCodeRepository.findByCode("CODE")).thenReturn(Optional.of(code));
        assertThatThrownBy(() -> discountService.apply("CODE", new BigDecimal("400.00")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void expiredCodeIsRejected() {
        DiscountCode code = code(DiscountType.PERCENT, "10");
        code.setValidTo(LocalDateTime.now().minusDays(1));
        when(discountCodeRepository.findByCode("CODE")).thenReturn(Optional.of(code));
        assertThatThrownBy(() -> discountService.apply("CODE", new BigDecimal("400.00")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void exhaustedCodeIsRejected() {
        DiscountCode code = code(DiscountType.PERCENT, "10");
        code.setMaxUses(2);
        code.setUsedCount(2);
        when(discountCodeRepository.findByCode("CODE")).thenReturn(Optional.of(code));
        assertThatThrownBy(() -> discountService.apply("CODE", new BigDecimal("400.00")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void unknownCodeIsRejected() {
        when(discountCodeRepository.findByCode("NOPE")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> discountService.apply("NOPE", new BigDecimal("400.00")))
                .isInstanceOf(BadRequestException.class);
    }
}
