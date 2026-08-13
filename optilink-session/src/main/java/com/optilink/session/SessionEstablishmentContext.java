package com.optilink.session;

import com.optilink.core.DeviceId;
import com.optilink.security.PeerRole;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Authenticated handshake result used to construct a secure session.
 *
 * @param sessionId negotiated session identifier
 * @param localDevice local device identity
 * @param remoteDevice remote device identity
 * @param role local peer role
 * @param transcript canonical transcript
 * @param sharedSecret X25519 shared secret
 * @param establishedAt establishment timestamp
 */
public record SessionEstablishmentContext(
        UUID sessionId,
        DeviceId localDevice,
        DeviceId remoteDevice,
        PeerRole role,
        HandshakeTranscript transcript,
        byte[] sharedSecret,
        Instant establishedAt) {

    /**
     * Creates a session establishment context.
     */
    public SessionEstablishmentContext {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(localDevice, "localDevice");
        Objects.requireNonNull(remoteDevice, "remoteDevice");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(transcript, "transcript");
        Objects.requireNonNull(sharedSecret, "sharedSecret");
        Objects.requireNonNull(establishedAt, "establishedAt");
        sharedSecret = sharedSecret.clone();
    }

    @Override
    public byte[] sharedSecret() {
        return sharedSecret.clone();
    }

    @Override
    public boolean equals(Object candidate) {
        return candidate instanceof SessionEstablishmentContext other
                && sessionId.equals(other.sessionId)
                && localDevice.equals(other.localDevice)
                && remoteDevice.equals(other.remoteDevice)
                && role == other.role
                && transcript.equals(other.transcript)
                && Arrays.equals(sharedSecret, other.sharedSecret)
                && establishedAt.equals(other.establishedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, localDevice, remoteDevice, role, transcript, Arrays.hashCode(sharedSecret), establishedAt);
    }
}
