package com.optilink.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Ephemeral X25519 key pair used for a single session or key rotation.
 *
 * @param publicKey agreement public key
 * @param privateKey agreement private key
 */
public record EphemeralKeyPair(PublicKey publicKey, PrivateKey privateKey) {

    /**
     * Creates an ephemeral key pair.
     */
    public EphemeralKeyPair {
        Objects.requireNonNull(publicKey, "publicKey");
        Objects.requireNonNull(privateKey, "privateKey");
    }
}
