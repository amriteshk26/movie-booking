package com.amritesh.moviebooking.exception;

/**
 * Thrown for invalid input or violated business rules. Mapped to HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
