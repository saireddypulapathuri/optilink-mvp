package com.optilink.server;

import java.time.Instant;
import java.util.Objects;

/**
 * Health response exposed by the server adapter.
 *
 * @param status service status
 * @param checkedAt response timestamp
 */
public record HealthSnapshot(String status, Instant checkedAt) {

    /**
     * Creates a health snapshot.
     */
    public HealthSnapshot {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(checkedAt, "checkedAt");
    }
}
