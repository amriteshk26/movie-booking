package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.request.DiscountCodeRequest;
import com.amritesh.moviebooking.dto.response.DiscountCodeResponse;
import com.amritesh.moviebooking.entity.DiscountCode;
import com.amritesh.moviebooking.entity.enums.DiscountType;
import com.amritesh.moviebooking.exception.BadRequestException;
import com.amritesh.moviebooking.exception.ResourceNotFoundException;
import com.amritesh.moviebooking.mapper.DiscountCodeMapper;
import com.amritesh.moviebooking.repository.DiscountCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin CRUD for discount codes plus the runtime application of a code to a
 * booking subtotal.
 */
@Service
public class DiscountService {

    private final DiscountCodeRepository discountCodeRepository;
    private final DiscountCodeMapper discountCodeMapper;

    public DiscountService(DiscountCodeRepository discountCodeRepository,
                           DiscountCodeMapper discountCodeMapper) {
        this.discountCodeRepository = discountCodeRepository;
        this.discountCodeMapper = discountCodeMapper;
    }

    /** Result of applying a discount: the amount deducted and the code used (may be null). */
    public record DiscountApplication(BigDecimal amount, DiscountCode code) {
    }

    // ---------------- Admin CRUD ----------------

    @Transactional
    public DiscountCodeResponse create(DiscountCodeRequest request) {
        discountCodeRepository.findByCode(request.code()).ifPresent(c -> {
            throw new BadRequestException("Discount code already exists: " + request.code());
        });
        if (request.discountType() == DiscountType.PERCENT
                && request.value().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Percent discount cannot exceed 100");
        }
        DiscountCode code = new DiscountCode();
        code.setCode(request.code());
        code.setDiscountType(request.discountType());
        code.setValue(request.value());
        code.setValidFrom(request.validFrom());
        code.setValidTo(request.validTo());
        code.setMaxUses(request.maxUses());
        code.setUsedCount(0);
        code.setActive(true);
        return discountCodeMapper.toResponse(discountCodeRepository.save(code));
    }

    @Transactional(readOnly = true)
    public List<DiscountCodeResponse> findAll() {
        return discountCodeRepository.findAll().stream().map(discountCodeMapper::toResponse).toList();
    }

    @Transactional
    public DiscountCodeResponse setActive(Long id, boolean active) {
        DiscountCode code = discountCodeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("DiscountCode", id));
        code.setActive(active);
        return discountCodeMapper.toResponse(code);
    }

    // ---------------- Runtime application ----------------

    /**
     * Validates the code and computes the discount for the given subtotal. Increments
     * the usage counter (optimistically locked). Returns a zero application when the
     * code is null/blank.
     */
    @Transactional
    public DiscountApplication apply(String rawCode, BigDecimal subtotal) {
        if (rawCode == null || rawCode.isBlank()) {
            return new DiscountApplication(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), null);
        }

        DiscountCode code = discountCodeRepository.findByCode(rawCode.trim())
                .orElseThrow(() -> new BadRequestException("Invalid discount code: " + rawCode));

        if (!code.isActive()) {
            throw new BadRequestException("Discount code is not active: " + rawCode);
        }
        LocalDateTime now = LocalDateTime.now();
        if (code.getValidFrom() != null && now.isBefore(code.getValidFrom())) {
            throw new BadRequestException("Discount code is not yet valid: " + rawCode);
        }
        if (code.getValidTo() != null && now.isAfter(code.getValidTo())) {
            throw new BadRequestException("Discount code has expired: " + rawCode);
        }
        if (code.getMaxUses() != null && code.getUsedCount() >= code.getMaxUses()) {
            throw new BadRequestException("Discount code usage limit reached: " + rawCode);
        }

        BigDecimal discount;
        if (code.getDiscountType() == DiscountType.PERCENT) {
            discount = subtotal.multiply(code.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = code.getValue();
        }
        // Never discount more than the subtotal.
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }
        discount = discount.setScale(2, RoundingMode.HALF_UP);

        code.setUsedCount(code.getUsedCount() + 1);
        // version increment on commit serializes concurrent redemptions

        return new DiscountApplication(discount, code);
    }
}
