package com.amritesh.moviebooking.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Convenience accessors for the currently authenticated principal.
 */
@Component
public class SecurityUtils {

    public CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return details;
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
