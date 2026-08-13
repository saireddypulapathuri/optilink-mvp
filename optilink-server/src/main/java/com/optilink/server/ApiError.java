package com.optilink.server;

import java.time.Instant;
import java.util.Objects;

/**
 * API error response.
 *
 * @param message user-facing error message
 * @param timestamp response timestamp
 */
public record ApiError(String message, Instant timestamp) {

    /**
     * Creates an API error.
     */
    public ApiError {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(timestamp, "timestamp");
    }
}
