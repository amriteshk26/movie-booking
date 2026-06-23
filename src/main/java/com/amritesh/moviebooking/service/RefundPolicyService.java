package com.amritesh.moviebooking.service;

import com.amritesh.moviebooking.dto.request.RefundPolicyRequest;
import com.amritesh.moviebooking.dto.response.RefundPolicyResponse;
import com.amritesh.moviebooking.entity.RefundPolicy;
import com.amritesh.moviebooking.exception.ResourceNotFoundException;
import com.amritesh.moviebooking.mapper.RefundPolicyMapper;
import com.amritesh.moviebooking.repository.RefundPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RefundPolicyService {

    private final RefundPolicyRepository refundPolicyRepository;
    private final RefundPolicyMapper refundPolicyMapper;

    public RefundPolicyService(RefundPolicyRepository refundPolicyRepository,
                               RefundPolicyMapper refundPolicyMapper) {
        this.refundPolicyRepository = refundPolicyRepository;
        this.refundPolicyMapper = refundPolicyMapper;
    }

    @Transactional
    public RefundPolicyResponse create(RefundPolicyRequest request) {
        RefundPolicy policy = new RefundPolicy();
        applyRequest(policy, request);
        if (request.isDefault()) {
            clearExistingDefault();
        }
        return refundPolicyMapper.toResponse(refundPolicyRepository.save(policy));
    }

    @Transactional
    public RefundPolicyResponse update(Long id, RefundPolicyRequest request) {
        RefundPolicy policy = refundPolicyRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("RefundPolicy", id));
        if (request.isDefault() && !policy.isDefault()) {
            clearExistingDefault();
        }
        applyRequest(policy, request);
        return refundPolicyMapper.toResponse(policy);
    }

    @Transactional(readOnly = true)
    public List<RefundPolicyResponse> findAll() {
        return refundPolicyRepository.findAll().stream().map(refundPolicyMapper::toResponse).toList();
    }

    private void applyRequest(RefundPolicy policy, RefundPolicyRequest request) {
        policy.setName(request.name());
        policy.setFullRefundHoursBefore(request.fullRefundHoursBefore());
        policy.setPartialRefundHoursBefore(request.partialRefundHoursBefore());
        policy.setFullRefundPercent(request.fullRefundPercent());
        policy.setPartialRefundPercent(request.partialRefundPercent());
        policy.setNoRefundPercent(request.noRefundPercent());
        policy.setDefault(request.isDefault());
    }

    private void clearExistingDefault() {
        refundPolicyRepository.findByIsDefaultTrue().ifPresent(existing -> existing.setDefault(false));
    }
}
