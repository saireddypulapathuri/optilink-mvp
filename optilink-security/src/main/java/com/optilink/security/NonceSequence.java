package com.optilink.security;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monotonic 96-bit nonce generator using a 32-bit session prefix and 64-bit counter.
 */
public final class NonceSequence {
    private static final int PREFIX_BYTES = 4;
    private final byte[] prefix;
    private final AtomicLong counter;

    /**
     * Creates a nonce sequence.
     *
     * @param prefix 32-bit session-unique prefix
     * @param initialCounter initial counter value
     */
    public NonceSequence(byte[] prefix, long initialCounter) {
        Objects.requireNonNull(prefix, "prefix");
        if (prefix.length != PREFIX_BYTES || initialCounter < 0) {
            throw new IllegalArgumentException("invalid nonce sequence parameters");
        }
        this.prefix = prefix.clone();
        this.counter = new AtomicLong(initialCounter);
    }

    /**
     * Returns the next unique nonce.
     *
     * @return 96-bit nonce
     */
    public byte[] nextNonce() {
        long value = counter.getAndIncrement();
        if (value < 0) {
            throw new IllegalStateException("nonce sequence exhausted");
        }
        ByteBuffer buffer = ByteBuffer.allocate(AesGcmPacketCipher.NONCE_BYTES);
        buffer.put(prefix);
        buffer.putLong(value);
        return buffer.array();
    }
}
