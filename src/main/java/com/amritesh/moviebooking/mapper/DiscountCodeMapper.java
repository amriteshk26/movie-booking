package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.DiscountCodeResponse;
import com.amritesh.moviebooking.entity.DiscountCode;
import org.springframework.stereotype.Component;

@Component
public class DiscountCodeMapper {

    public DiscountCodeResponse toResponse(DiscountCode code) {
        return new DiscountCodeResponse(
                code.getId(),
                code.getCode(),
                code.getDiscountType(),
                code.getValue(),
                code.getValidFrom(),
                code.getValidTo(),
                code.getMaxUses(),
                code.getUsedCount(),
                code.isActive()
        );
    }
}
