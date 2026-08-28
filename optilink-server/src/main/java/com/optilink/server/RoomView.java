package com.optilink.server;

import java.time.Instant;
import java.util.Objects;

/**
 * Browser-facing transfer room state.
 *
 * @param code short room code
 * @param expiresAt room expiry time
 * @param transferCount transfers currently attached to the room
 * @param receiveUrl receiver URL carrying the room code
 */
public record RoomView(String code, Instant expiresAt, int transferCount, String receiveUrl) {

    /**
     * Creates a room view.
     */
    public RoomView {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(receiveUrl, "receiveUrl");
    }
}
