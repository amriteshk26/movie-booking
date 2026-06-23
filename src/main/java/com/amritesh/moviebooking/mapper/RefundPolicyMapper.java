package com.amritesh.moviebooking.mapper;

import com.amritesh.moviebooking.dto.response.RefundPolicyResponse;
import com.amritesh.moviebooking.entity.RefundPolicy;
import org.springframework.stereotype.Component;

@Component
public class RefundPolicyMapper {

    public RefundPolicyResponse toResponse(RefundPolicy policy) {
        return new RefundPolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getFullRefundHoursBefore(),
                policy.getPartialRefundHoursBefore(),
                policy.getFullRefundPercent(),
                policy.getPartialRefundPercent(),
                policy.getNoRefundPercent(),
                policy.isDefault()
        );
    }
}
