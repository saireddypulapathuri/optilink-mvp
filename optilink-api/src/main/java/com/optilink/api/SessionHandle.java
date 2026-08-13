package com.optilink.api;

import java.util.Objects;
import java.util.UUID;

/**
 * Application-level reference to an OptiLink session.
 *
 * @param sessionId secure session identifier
 */
public record SessionHandle(UUID sessionId) {

    /**
     * Creates a session handle.
     */
    public SessionHandle {
        Objects.requireNonNull(sessionId, "sessionId");
    }
}
