package com.optilink.session;

import com.optilink.core.DeviceId;
import com.optilink.security.HkdfSha256;
import com.optilink.security.PeerRole;
import com.optilink.security.SessionKeyDeriver;
import com.optilink.security.SessionKeys;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.UUID;

/**
 * Builds established session descriptors from authenticated handshake material.
 */
public final class SecureSessionFactory {
    private final SessionKeyDeriver keyDeriver;
    private final SessionPolicy policy;

    /**
     * Creates a session factory with production defaults.
     */
    public SecureSessionFactory() {
        this(new SessionKeyDeriver(new HkdfSha256()), SessionPolicy.productionDefault());
    }

    /**
     * Creates a session factory.
     *
     * @param keyDeriver session key deriver
     * @param policy session policy
     */
    public SecureSessionFactory(SessionKeyDeriver keyDeriver, SessionPolicy policy) {
        this.keyDeriver = Objects.requireNonNull(keyDeriver, "keyDeriver");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    /**
     * Establishes a secure session after the caller validates peer signatures.
     *
     * @param context establishment context
     * @return established secure session
     * @throws GeneralSecurityException when key derivation fails
     */
    public SecureSession establish(SessionEstablishmentContext context) throws GeneralSecurityException {
        Objects.requireNonNull(context, "context");
        SessionKeys keys = keyDeriver.derive(context.sharedSecret(), context.transcript().sha256(), context.role());
        var descriptor = new SessionDescriptor(
                context.sessionId(),
                context.localDevice(),
                context.remoteDevice(),
                context.establishedAt());
        return new SecureSession(descriptor, keys, policy, SessionState.ESTABLISHED, context.establishedAt());
    }

    /**
     * Creates a random session identifier.
     *
     * @return session identifier
     */
    public UUID newSessionId() {
        return UUID.randomUUID();
    }
}
