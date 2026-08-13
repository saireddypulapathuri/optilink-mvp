package com.optilink.security;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Objects;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Derives AES-256 keys from operator-provided passphrases for MVP sealed transfers.
 */
public final class PassphraseKeyDeriver {
    /**
     * Default PBKDF2 iteration count for MVP transfers.
     */
    public static final int DEFAULT_ITERATIONS = 120_000;

    private static final String PBKDF2 = "PBKDF2WithHmacSHA256";
    private static final int KEY_BITS = AesGcmPacketCipher.KEY_BYTES * 8;

    /**
     * Derives a 256-bit key from a passphrase.
     *
     * @param passphrase operator-provided passphrase
     * @param salt random salt
     * @param iterations PBKDF2 iteration count
     * @return derived key bytes
     * @throws GeneralSecurityException when PBKDF2 is unavailable
     */
    public byte[] derive(char[] passphrase, byte[] salt, int iterations) throws GeneralSecurityException {
        Objects.requireNonNull(passphrase, "passphrase");
        Objects.requireNonNull(salt, "salt");
        if (passphrase.length < 8 || salt.length < 16 || iterations < 10_000) {
            throw new IllegalArgumentException("passphrase, salt, or iteration policy is too weak");
        }
        char[] passphraseCopy = passphrase.clone();
        PBEKeySpec keySpec = new PBEKeySpec(passphraseCopy, salt.clone(), iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(PBKDF2).generateSecret(keySpec).getEncoded();
        } finally {
            keySpec.clearPassword();
            Arrays.fill(passphraseCopy, '\0');
        }
    }
}
