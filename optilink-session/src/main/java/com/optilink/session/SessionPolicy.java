package com.optilink.session;

import java.time.Duration;
import java.util.Objects;

/**
 * Security and operational limits applied to every session.
 *
 * @param keyRotationInterval maximum lifetime of traffic keys
 * @param replayWindowSize replay-protection window size
 * @param maxUnauthenticatedHandshakeMillis maximum time before handshake completion
 */
public record SessionPolicy(
        Duration keyRotationInterval,
        int replayWindowSize,
        long maxUnauthenticatedHandshakeMillis) {

    /**
     * Creates a validated session policy.
     */
    public SessionPolicy {
        Objects.requireNonNull(keyRotationInterval, "keyRotationInterval");
        if (keyRotationInterval.isNegative() || keyRotationInterval.isZero()) {
            throw new IllegalArgumentException("keyRotationInterval must be positive");
        }
        if (replayWindowSize < 1 || maxUnauthenticatedHandshakeMillis < 1) {
            throw new IllegalArgumentException("session policy limits must be positive");
        }
    }

    /**
     * Returns conservative production defaults.
     *
     * @return default session policy
     */
    public static SessionPolicy productionDefault() {
        return new SessionPolicy(Duration.ofMinutes(15), 4_096, 30_000);
    }
}
