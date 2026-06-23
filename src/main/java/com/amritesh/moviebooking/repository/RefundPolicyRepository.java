package com.amritesh.moviebooking.repository;

import com.amritesh.moviebooking.entity.RefundPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

    Optional<RefundPolicy> findByIsDefaultTrue();
}
