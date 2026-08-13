package com.optilink.session;

import com.optilink.security.SessionKeys;
import java.time.Instant;
import java.util.Objects;

/**
 * Established secure session with directional traffic keys and lifecycle state.
 *
 * @param descriptor session metadata
 * @param keys directional traffic keys
 * @param policy session policy
 * @param state lifecycle state
 * @param lastKeyRotationAt last key rotation timestamp
 */
public record SecureSession(
        SessionDescriptor descriptor,
        SessionKeys keys,
        SessionPolicy policy,
        SessionState state,
        Instant lastKeyRotationAt) {

    /**
     * Creates a secure session.
     */
    public SecureSession {
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(keys, "keys");
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(lastKeyRotationAt, "lastKeyRotationAt");
    }

    /**
     * Returns whether traffic keys should be rotated at the supplied instant.
     *
     * @param now current time
     * @return true when key rotation is due
     */
    public boolean requiresKeyRotation(Instant now) {
        Objects.requireNonNull(now, "now");
        return !lastKeyRotationAt.plus(policy.keyRotationInterval()).isAfter(now);
    }
}
