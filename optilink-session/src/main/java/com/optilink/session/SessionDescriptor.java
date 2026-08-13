package com.optilink.session;

import com.optilink.core.DeviceId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Stable metadata for an established session.
 *
 * @param sessionId unique session identifier
 * @param localDevice local device identity
 * @param remoteDevice remote device identity
 * @param establishedAt establishment timestamp
 */
public record SessionDescriptor(
        UUID sessionId,
        DeviceId localDevice,
        DeviceId remoteDevice,
        Instant establishedAt) {

    /**
     * Creates a session descriptor.
     */
    public SessionDescriptor {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(localDevice, "localDevice");
        Objects.requireNonNull(remoteDevice, "remoteDevice");
        Objects.requireNonNull(establishedAt, "establishedAt");
    }
}
