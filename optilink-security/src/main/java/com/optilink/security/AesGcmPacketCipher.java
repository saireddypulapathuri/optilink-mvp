package com.optilink.security;

import java.security.GeneralSecurityException;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256-GCM packet cipher for authenticated encryption.
 */
public final class AesGcmPacketCipher {
    /**
     * AES-256 key length in bytes.
     */
    public static final int KEY_BYTES = 32;

    /**
     * Recommended GCM nonce length in bytes.
     */
    public static final int NONCE_BYTES = 12;

    private static final int GCM_TAG_BITS = 128;
    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";

    /**
     * Encrypts plaintext with associated data.
     *
     * @param key 256-bit AES key
     * @param nonce 96-bit GCM nonce
     * @param plaintext plaintext bytes
     * @param associatedData authenticated associated data
     * @return encrypted packet
     * @throws GeneralSecurityException when encryption fails
     */
    public EncryptedPacket encrypt(byte[] key, byte[] nonce, byte[] plaintext, byte[] associatedData)
            throws GeneralSecurityException {
        Cipher cipher = initialize(Cipher.ENCRYPT_MODE, key, nonce, associatedData);
        return new EncryptedPacket(nonce, cipher.doFinal(plaintext.clone()));
    }

    /**
     * Decrypts ciphertext with associated data.
     *
     * @param key 256-bit AES key
     * @param packet encrypted packet
     * @param associatedData authenticated associated data
     * @return plaintext bytes
     * @throws GeneralSecurityException when authentication or decryption fails
     */
    public byte[] decrypt(byte[] key, EncryptedPacket packet, byte[] associatedData)
            throws GeneralSecurityException {
        Cipher cipher = initialize(Cipher.DECRYPT_MODE, key, packet.nonce(), associatedData);
        return cipher.doFinal(packet.ciphertext());
    }

    private Cipher initialize(int mode, byte[] key, byte[] nonce, byte[] associatedData)
            throws GeneralSecurityException {
        validateInputs(key, nonce, associatedData);
        Cipher cipher = Cipher.getInstance(AES_GCM);
        SecretKeySpec keySpec = new SecretKeySpec(key.clone(), AES);
        cipher.init(mode, keySpec, new GCMParameterSpec(GCM_TAG_BITS, nonce.clone()));
        cipher.updateAAD(associatedData.clone());
        return cipher;
    }

    private void validateInputs(byte[] key, byte[] nonce, byte[] associatedData) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(nonce, "nonce");
        Objects.requireNonNull(associatedData, "associatedData");
        if (key.length != KEY_BYTES) {
            throw new IllegalArgumentException("AES-GCM key must be 256-bit");
        }
        if (nonce.length != NONCE_BYTES) {
            throw new IllegalArgumentException("AES-GCM nonce must be 96-bit");
        }
    }
}
