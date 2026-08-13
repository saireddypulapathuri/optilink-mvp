package com.optilink.security;

import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * RFC 5869 HKDF implementation using HMAC-SHA-256.
 */
public final class HkdfSha256 {
    /**
     * HMAC-SHA-256 output size in bytes.
     */
    public static final int HASH_BYTES = 32;

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final int MAX_BLOCKS = 255;

    /**
     * Performs HKDF extract.
     *
     * @param salt salt bytes; empty salt is replaced with hash-length zero bytes
     * @param inputKeyMaterial input key material
     * @return pseudorandom key
     * @throws GeneralSecurityException when HMAC is unavailable
     */
    public byte[] extract(byte[] salt, byte[] inputKeyMaterial) throws GeneralSecurityException {
        Objects.requireNonNull(salt, "salt");
        Objects.requireNonNull(inputKeyMaterial, "inputKeyMaterial");
        byte[] actualSalt = salt.length == 0 ? new byte[HASH_BYTES] : salt.clone();
        return hmac(actualSalt, inputKeyMaterial);
    }

    /**
     * Performs HKDF expand.
     *
     * @param pseudorandomKey HKDF pseudorandom key
     * @param info context binding information
     * @param length output length in bytes
     * @return output key material
     * @throws GeneralSecurityException when HMAC is unavailable
     */
    public byte[] expand(byte[] pseudorandomKey, byte[] info, int length) throws GeneralSecurityException {
        validateExpandInputs(pseudorandomKey, info, length);
        ByteArrayOutputStream output = new ByteArrayOutputStream(length);
        byte[] previous = new byte[0];
        for (int block = 1; output.size() < length; block++) {
            previous = nextBlock(pseudorandomKey, previous, info, block);
            output.writeBytes(previous);
        }
        return Arrays.copyOf(output.toByteArray(), length);
    }

    /**
     * Performs HKDF extract followed by expand.
     *
     * @param salt salt bytes
     * @param inputKeyMaterial input key material
     * @param info context binding information
     * @param length output length in bytes
     * @return output key material
     * @throws GeneralSecurityException when HMAC is unavailable
     */
    public byte[] derive(byte[] salt, byte[] inputKeyMaterial, byte[] info, int length)
            throws GeneralSecurityException {
        return expand(extract(salt, inputKeyMaterial), info, length);
    }

    private void validateExpandInputs(byte[] pseudorandomKey, byte[] info, int length) {
        Objects.requireNonNull(pseudorandomKey, "pseudorandomKey");
        Objects.requireNonNull(info, "info");
        if (length < 0 || length > HASH_BYTES * MAX_BLOCKS) {
            throw new IllegalArgumentException("invalid HKDF output length");
        }
    }

    private byte[] nextBlock(byte[] pseudorandomKey, byte[] previous, byte[] info, int block)
            throws GeneralSecurityException {
        byte[] blockInput = new byte[previous.length + info.length + 1];
        System.arraycopy(previous, 0, blockInput, 0, previous.length);
        System.arraycopy(info, 0, blockInput, previous.length, info.length);
        blockInput[blockInput.length - 1] = (byte) block;
        return hmac(pseudorandomKey, blockInput);
    }

    private byte[] hmac(byte[] key, byte[] message) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(new SecretKeySpec(key.clone(), HMAC_SHA_256));
        return mac.doFinal(message.clone());
    }
}
