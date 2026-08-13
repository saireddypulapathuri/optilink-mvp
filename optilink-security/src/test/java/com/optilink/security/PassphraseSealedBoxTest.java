package com.optilink.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import org.junit.jupiter.api.Test;

class PassphraseSealedBoxTest {

    @Test
    void sealedPayloadRoundTripsAndRejectsWrongPassphrase() throws Exception {
        PassphraseSealedBox sealedBox = new PassphraseSealedBox();
        byte[] plaintext = "offline optical MVP".getBytes(StandardCharsets.UTF_8);
        char[] passphrase = "correct horse battery staple".toCharArray();

        SealedPayload sealedPayload = sealedBox.seal(plaintext, passphrase);
        SealedPayload decoded = sealedBox.decode(sealedBox.encode(sealedPayload));

        assertArrayEquals(plaintext, sealedBox.open(decoded, passphrase));
        assertThrows(GeneralSecurityException.class, () -> sealedBox.open(decoded, "wrong passphrase".toCharArray()));
    }
}
