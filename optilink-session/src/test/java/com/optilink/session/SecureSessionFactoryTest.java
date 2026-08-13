package com.optilink.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.optilink.core.DeviceId;
import com.optilink.security.PeerRole;
import com.optilink.security.X25519KeyAgreementService;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SecureSessionFactoryTest {

    @Test
    void establishesSessionAndDetectsKeyRotationDeadline() throws Exception {
        X25519KeyAgreementService agreement = new X25519KeyAgreementService();
        var localKey = agreement.generateEphemeral();
        var remoteKey = agreement.generateEphemeral();
        DeviceId localDevice = new DeviceId(UUID.randomUUID());
        DeviceId remoteDevice = new DeviceId(UUID.randomUUID());
        var transcript = new HandshakeTranscript(localDevice, remoteDevice, localKey.publicKey(), remoteKey.publicKey());
        byte[] secret = agreement.agree(localKey.privateKey(), remoteKey.publicKey());
        Instant establishedAt = Instant.EPOCH;
        var policy = new SessionPolicy(Duration.ofSeconds(5), 64, 1_000);
        SecureSessionFactory factory = new SecureSessionFactory(new com.optilink.security.SessionKeyDeriver(new com.optilink.security.HkdfSha256()), policy);

        SecureSession session = factory.establish(new SessionEstablishmentContext(
                UUID.randomUUID(),
                localDevice,
                remoteDevice,
                PeerRole.INITIATOR,
                transcript,
                secret,
                establishedAt));

        assertFalse(session.requiresKeyRotation(establishedAt.plusSeconds(4)));
        assertTrue(session.requiresKeyRotation(establishedAt.plusSeconds(5)));
    }
}
