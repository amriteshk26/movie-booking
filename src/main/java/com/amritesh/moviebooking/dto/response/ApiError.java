package com.amritesh.moviebooking.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Consistent error envelope returned by the global exception handler.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
