package com.optilink.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Long-lived Ed25519 identity key pair.
 *
 * @param publicKey verification key
 * @param privateKey signing key
 */
public record IdentityKeyPair(PublicKey publicKey, PrivateKey privateKey) {

    /**
     * Creates an identity key pair.
     */
    public IdentityKeyPair {
        Objects.requireNonNull(publicKey, "publicKey");
        Objects.requireNonNull(privateKey, "privateKey");
    }
}
