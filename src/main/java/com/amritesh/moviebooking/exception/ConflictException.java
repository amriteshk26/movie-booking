package com.amritesh.moviebooking.exception;

/**
 * Thrown when a concurrent modification or state conflict occurs (e.g. a seat
 * was taken by another user). Mapped to HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
