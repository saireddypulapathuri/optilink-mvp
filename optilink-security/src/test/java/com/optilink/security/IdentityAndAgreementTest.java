package com.optilink.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class IdentityAndAgreementTest {

    @Test
    void identitySignaturesAndX25519AgreementWork() throws Exception {
        IdentityKeyService identityService = new IdentityKeyService();
        IdentityKeyPair identity = identityService.generate();
        byte[] transcript = "handshake transcript".getBytes(StandardCharsets.UTF_8);
        byte[] signature = identityService.sign(identity.privateKey(), transcript);

        X25519KeyAgreementService agreement = new X25519KeyAgreementService();
        EphemeralKeyPair first = agreement.generateEphemeral();
        EphemeralKeyPair second = agreement.generateEphemeral();

        assertTrue(identityService.verify(identity.publicKey(), transcript, signature));
        assertFalse(identityService.verify(identity.publicKey(), new byte[] {1}, signature));
        assertArrayEquals(
                agreement.agree(first.privateKey(), second.publicKey()),
                agreement.agree(second.privateKey(), first.publicKey()));
    }
}
