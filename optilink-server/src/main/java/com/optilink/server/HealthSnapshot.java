package com.optilink.server;

import java.time.Instant;
import java.util.Objects;

/**
 * Health response exposed by the server adapter.
 *
 * @param status service status
 * @param checkedAt response timestamp
 * @param activeRooms active pairing rooms
 * @param activeScans active camera scan sessions
 */
public record HealthSnapshot(String status, Instant checkedAt, int activeRooms, int activeScans) {

    /**
     * Creates a health snapshot.
     */
    public HealthSnapshot {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(checkedAt, "checkedAt");
        if (activeRooms < 0 || activeScans < 0) {
            throw new IllegalArgumentException("active counts cannot be negative");
        }
    }
}
