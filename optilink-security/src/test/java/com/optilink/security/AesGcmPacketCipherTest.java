package com.optilink.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import org.junit.jupiter.api.Test;

class AesGcmPacketCipherTest {

    @Test
    void encryptsAndAuthenticatesAssociatedData() throws Exception {
        AesGcmPacketCipher cipher = new AesGcmPacketCipher();
        byte[] key = key();
        byte[] nonce = new NonceSequence(new byte[] {1, 2, 3, 4}, 0).nextNonce();
        byte[] plaintext = "secret".getBytes(StandardCharsets.UTF_8);
        byte[] aad = "header".getBytes(StandardCharsets.UTF_8);

        EncryptedPacket encrypted = cipher.encrypt(key, nonce, plaintext, aad);

        assertArrayEquals(plaintext, cipher.decrypt(key, encrypted, aad));
        assertThrows(GeneralSecurityException.class, () -> cipher.decrypt(key, encrypted, new byte[] {9}));
    }

    private byte[] key() {
        byte[] key = new byte[AesGcmPacketCipher.KEY_BYTES];
        for (int index = 0; index < key.length; index++) {
            key[index] = (byte) index;
        }
        return key;
    }
}
