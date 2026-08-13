package com.optilink.security;

import java.util.Arrays;
import java.util.Objects;

/**
 * Passphrase-sealed payload metadata and ciphertext.
 *
 * @param iterations PBKDF2 iteration count
 * @param salt random key-derivation salt
 * @param nonce AES-GCM nonce
 * @param ciphertext ciphertext with authentication tag
 */
public record SealedPayload(int iterations, byte[] salt, byte[] nonce, byte[] ciphertext) {

    /**
     * Creates a sealed payload.
     */
    public SealedPayload {
        Objects.requireNonNull(salt, "salt");
        Objects.requireNonNull(nonce, "nonce");
        Objects.requireNonNull(ciphertext, "ciphertext");
        if (iterations < 10_000 || salt.length < 16 || nonce.length != AesGcmPacketCipher.NONCE_BYTES) {
            throw new IllegalArgumentException("invalid sealed payload parameters");
        }
        salt = salt.clone();
        nonce = nonce.clone();
        ciphertext = ciphertext.clone();
    }

    @Override
    public byte[] salt() {
        return salt.clone();
    }

    @Override
    public byte[] nonce() {
        return nonce.clone();
    }

    @Override
    public byte[] ciphertext() {
        return ciphertext.clone();
    }

    @Override
    public boolean equals(Object candidate) {
        return candidate instanceof SealedPayload other
                && iterations == other.iterations
                && Arrays.equals(salt, other.salt)
                && Arrays.equals(nonce, other.nonce)
                && Arrays.equals(ciphertext, other.ciphertext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iterations, Arrays.hashCode(salt), Arrays.hashCode(nonce), Arrays.hashCode(ciphertext));
    }
}
