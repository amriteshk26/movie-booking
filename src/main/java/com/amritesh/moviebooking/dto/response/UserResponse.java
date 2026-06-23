package com.amritesh.moviebooking.dto.response;

import com.amritesh.moviebooking.entity.enums.Role;

import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        Set<Role> roles
) {
}
