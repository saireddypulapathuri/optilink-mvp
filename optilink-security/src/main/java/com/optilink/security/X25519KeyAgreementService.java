package com.optilink.security;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.KeyAgreement;

/**
 * Performs X25519 ephemeral Diffie-Hellman agreement.
 */
public final class X25519KeyAgreementService {
    private static final String X25519 = "X25519";

    /**
     * Generates an ephemeral X25519 key pair.
     *
     * @return generated key pair
     * @throws GeneralSecurityException when key generation fails
     */
    public EphemeralKeyPair generateEphemeral() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(X25519);
        KeyPair keyPair = generator.generateKeyPair();
        return new EphemeralKeyPair(keyPair.getPublic(), keyPair.getPrivate());
    }

    /**
     * Computes a shared secret from private and peer public key material.
     *
     * @param privateKey local private key
     * @param peerPublicKey peer public key
     * @return shared secret bytes
     * @throws GeneralSecurityException when agreement fails
     */
    public byte[] agree(PrivateKey privateKey, PublicKey peerPublicKey) throws GeneralSecurityException {
        KeyAgreement agreement = KeyAgreement.getInstance(X25519);
        agreement.init(privateKey);
        agreement.doPhase(peerPublicKey, true);
        return agreement.generateSecret();
    }
}
