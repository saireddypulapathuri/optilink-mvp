package com.optilink.core;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable binary payload used at protocol, security, and transport boundaries.
 *
 * @param bytes payload bytes; copied on input and output
 */
public record BinaryPayload(byte[] bytes) {

    /**
     * Creates an immutable payload.
     *
     * @param bytes payload bytes
     */
    public BinaryPayload {
        Objects.requireNonNull(bytes, "bytes");
        bytes = bytes.clone();
    }

    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    /**
     * Returns the number of bytes in the payload.
     *
     * @return payload length in bytes
     */
    public int length() {
        return bytes.length;
    }

    /**
     * Returns whether this payload has no bytes.
     *
     * @return true when the payload is empty
     */
    public boolean isEmpty() {
        return bytes.length == 0;
    }

    @Override
    public boolean equals(Object candidate) {
        return candidate instanceof BinaryPayload other
                && Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return "BinaryPayload[length=" + bytes.length + "]";
    }
}
