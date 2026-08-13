package com.optilink.security;

import com.optilink.core.EntropySource;
import com.optilink.core.SecureEntropySource;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Objects;

/**
 * AES-GCM sealed box using PBKDF2-derived keys for practical offline MVP transfers.
 */
public final class PassphraseSealedBox {
    private static final int MAGIC = 0x4F534C44;
    private static final int VERSION = 1;
    private static final int SALT_BYTES = 16;
    private static final byte[] ASSOCIATED_DATA = "OptiLink-MVP-SealedPayload-v1".getBytes(StandardCharsets.US_ASCII);
    private final PassphraseKeyDeriver keyDeriver;
    private final AesGcmPacketCipher cipher;
    private final EntropySource entropySource;

    /**
     * Creates a sealed box with production entropy.
     */
    public PassphraseSealedBox() {
        this(new PassphraseKeyDeriver(), new AesGcmPacketCipher(), new SecureEntropySource());
    }

    /**
     * Creates a sealed box.
     *
     * @param keyDeriver passphrase key deriver
     * @param cipher packet cipher
     * @param entropySource entropy source
     */
    public PassphraseSealedBox(
            PassphraseKeyDeriver keyDeriver,
            AesGcmPacketCipher cipher,
            EntropySource entropySource) {
        this.keyDeriver = Objects.requireNonNull(keyDeriver, "keyDeriver");
        this.cipher = Objects.requireNonNull(cipher, "cipher");
        this.entropySource = Objects.requireNonNull(entropySource, "entropySource");
    }

    /**
     * Encrypts plaintext with a passphrase-derived AES-GCM key.
     *
     * @param plaintext plaintext bytes
     * @param passphrase operator passphrase
     * @return sealed payload
     * @throws GeneralSecurityException when encryption fails
     */
    public SealedPayload seal(byte[] plaintext, char[] passphrase) throws GeneralSecurityException {
        byte[] salt = entropySource.randomBytes(SALT_BYTES);
        byte[] nonce = entropySource.randomBytes(AesGcmPacketCipher.NONCE_BYTES);
        byte[] key = keyDeriver.derive(passphrase, salt, PassphraseKeyDeriver.DEFAULT_ITERATIONS);
        try {
            EncryptedPacket packet = cipher.encrypt(key, nonce, plaintext.clone(), ASSOCIATED_DATA);
            return new SealedPayload(PassphraseKeyDeriver.DEFAULT_ITERATIONS, salt, packet.nonce(), packet.ciphertext());
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    /**
     * Decrypts a sealed payload.
     *
     * @param sealedPayload sealed payload
     * @param passphrase operator passphrase
     * @return plaintext bytes
     * @throws GeneralSecurityException when authentication or decryption fails
     */
    public byte[] open(SealedPayload sealedPayload, char[] passphrase) throws GeneralSecurityException {
        byte[] key = keyDeriver.derive(passphrase, sealedPayload.salt(), sealedPayload.iterations());
        try {
            return cipher.decrypt(key, new EncryptedPacket(sealedPayload.nonce(), sealedPayload.ciphertext()), ASSOCIATED_DATA);
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    /**
     * Encodes a sealed payload for transport chunking.
     *
     * @param sealedPayload sealed payload
     * @return encoded bytes
     */
    public byte[] encode(SealedPayload sealedPayload) {
        byte[] salt = sealedPayload.salt();
        byte[] nonce = sealedPayload.nonce();
        byte[] ciphertext = sealedPayload.ciphertext();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 6 + salt.length + nonce.length + ciphertext.length);
        buffer.putInt(MAGIC);
        buffer.putInt(VERSION);
        buffer.putInt(sealedPayload.iterations());
        putBytes(buffer, salt);
        putBytes(buffer, nonce);
        putBytes(buffer, ciphertext);
        return buffer.array();
    }

    /**
     * Decodes a sealed payload from transport bytes.
     *
     * @param encoded encoded sealed payload
     * @return decoded sealed payload
     */
    public SealedPayload decode(byte[] encoded) {
        ByteBuffer buffer = ByteBuffer.wrap(encoded.clone());
        verifyHeader(buffer);
        int iterations = buffer.getInt();
        byte[] salt = readBytes(buffer);
        byte[] nonce = readBytes(buffer);
        byte[] ciphertext = readBytes(buffer);
        if (buffer.hasRemaining()) {
            throw new IllegalArgumentException("sealed payload contains trailing bytes");
        }
        return new SealedPayload(iterations, salt, nonce, ciphertext);
    }

    private void putBytes(ByteBuffer buffer, byte[] bytes) {
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }

    private byte[] readBytes(ByteBuffer buffer) {
        int length = buffer.getInt();
        if (length < 0 || length > buffer.remaining()) {
            throw new IllegalArgumentException("invalid sealed payload length");
        }
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    private void verifyHeader(ByteBuffer buffer) {
        if (buffer.remaining() < Integer.BYTES * 6 || buffer.getInt() != MAGIC || buffer.getInt() != VERSION) {
            throw new IllegalArgumentException("invalid sealed payload header");
        }
    }
}
