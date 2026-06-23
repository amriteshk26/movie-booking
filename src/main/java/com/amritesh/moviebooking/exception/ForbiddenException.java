package com.amritesh.moviebooking.exception;

/**
 * Thrown when an authenticated user attempts to access a resource they do not
 * own. Mapped to HTTP 403.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
