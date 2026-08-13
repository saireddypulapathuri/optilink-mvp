package com.optilink.security;

import java.util.Arrays;
import java.util.Objects;

/**
 * AEAD output containing nonce and ciphertext with authentication tag.
 *
 * @param nonce 96-bit GCM nonce
 * @param ciphertext ciphertext and authentication tag
 */
public record EncryptedPacket(byte[] nonce, byte[] ciphertext) {

    /**
     * Creates encrypted packet material.
     */
    public EncryptedPacket {
        Objects.requireNonNull(nonce, "nonce");
        Objects.requireNonNull(ciphertext, "ciphertext");
        nonce = nonce.clone();
        ciphertext = ciphertext.clone();
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
        return candidate instanceof EncryptedPacket other
                && Arrays.equals(nonce, other.nonce)
                && Arrays.equals(ciphertext, other.ciphertext);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(nonce) + Arrays.hashCode(ciphertext);
    }
}
