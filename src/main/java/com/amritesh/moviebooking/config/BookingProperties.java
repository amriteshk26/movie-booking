package com.amritesh.moviebooking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code booking.hold.*} configuration keys.
 */
@ConfigurationProperties(prefix = "booking.hold")
public class BookingProperties {

    /** How long a seat hold remains valid before automatic release. */
    private long ttlSeconds = 300;

    /** How often the expiry sweeper runs, in milliseconds. */
    private long sweepIntervalMs = 30000;

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public long getSweepIntervalMs() {
        return sweepIntervalMs;
    }

    public void setSweepIntervalMs(long sweepIntervalMs) {
        this.sweepIntervalMs = sweepIntervalMs;
    }
}
