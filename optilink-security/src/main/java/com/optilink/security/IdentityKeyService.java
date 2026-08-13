package com.optilink.security;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * Generates and uses Ed25519 identity keys.
 */
public final class IdentityKeyService {
    private static final String ED25519 = "Ed25519";

    /**
     * Generates a new Ed25519 identity key pair.
     *
     * @return generated identity key pair
     * @throws GeneralSecurityException when the provider cannot generate keys
     */
    public IdentityKeyPair generate() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ED25519);
        KeyPair keyPair = generator.generateKeyPair();
        return new IdentityKeyPair(keyPair.getPublic(), keyPair.getPrivate());
    }

    /**
     * Signs transcript bytes with an Ed25519 private key.
     *
     * @param privateKey signing key
     * @param transcript transcript to sign
     * @return signature bytes
     * @throws GeneralSecurityException when signing fails
     */
    public byte[] sign(PrivateKey privateKey, byte[] transcript) throws GeneralSecurityException {
        Signature signature = Signature.getInstance(ED25519);
        signature.initSign(privateKey);
        signature.update(transcript.clone());
        return signature.sign();
    }

    /**
     * Verifies transcript bytes with an Ed25519 public key.
     *
     * @param publicKey verification key
     * @param transcript signed transcript
     * @param signedTranscript signature bytes
     * @return true when the signature is valid
     * @throws GeneralSecurityException when verification cannot be performed
     */
    public boolean verify(PublicKey publicKey, byte[] transcript, byte[] signedTranscript)
            throws GeneralSecurityException {
        Signature signature = Signature.getInstance(ED25519);
        signature.initVerify(publicKey);
        signature.update(transcript.clone());
        return signature.verify(signedTranscript.clone());
    }
}
