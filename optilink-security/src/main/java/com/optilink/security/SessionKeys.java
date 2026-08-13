package com.optilink.security;

import java.util.Arrays;
import java.util.Objects;

/**
 * Directional traffic keys derived from a session handshake.
 *
 * @param outboundKey key used for packets sent by this peer
 * @param inboundKey key used for packets received by this peer
 */
public record SessionKeys(byte[] outboundKey, byte[] inboundKey) {

    /**
     * Creates a directional key set.
     */
    public SessionKeys {
        Objects.requireNonNull(outboundKey, "outboundKey");
        Objects.requireNonNull(inboundKey, "inboundKey");
        if (outboundKey.length != AesGcmPacketCipher.KEY_BYTES || inboundKey.length != AesGcmPacketCipher.KEY_BYTES) {
            throw new IllegalArgumentException("traffic keys must be 256-bit");
        }
        outboundKey = outboundKey.clone();
        inboundKey = inboundKey.clone();
    }

    @Override
    public byte[] outboundKey() {
        return outboundKey.clone();
    }

    @Override
    public byte[] inboundKey() {
        return inboundKey.clone();
    }

    @Override
    public boolean equals(Object candidate) {
        return candidate instanceof SessionKeys other
                && Arrays.equals(outboundKey, other.outboundKey)
                && Arrays.equals(inboundKey, other.inboundKey);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(outboundKey) + Arrays.hashCode(inboundKey);
    }
}
