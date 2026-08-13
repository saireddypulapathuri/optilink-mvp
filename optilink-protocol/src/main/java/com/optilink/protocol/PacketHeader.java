package com.optilink.protocol;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable packet header carried by every OptiLink frame.
 *
 * @param version protocol version
 * @param type packet type
 * @param flags unsigned 16-bit packet flags
 * @param sessionId session identifier
 * @param sequenceNumber monotonically increasing sender sequence number
 * @param acknowledgementNumber highest contiguous received peer sequence
 * @param payloadLength payload length in bytes
 * @param checksum CRC32C over canonical header fields and payload
 */
public record PacketHeader(
        int version,
        PacketType type,
        int flags,
        UUID sessionId,
        long sequenceNumber,
        long acknowledgementNumber,
        int payloadLength,
        int checksum) {

    /**
     * Creates a validated packet header.
     */
    public PacketHeader {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(sessionId, "sessionId");
        if (version != BinaryPacketCodec.CURRENT_VERSION) {
            throw new IllegalArgumentException("unsupported version: " + version);
        }
        if (flags < 0 || flags > BinaryPacketCodec.MAX_FLAGS) {
            throw new IllegalArgumentException("flags must fit in 16 bits");
        }
        if (sequenceNumber < 0 || acknowledgementNumber < 0) {
            throw new IllegalArgumentException("sequence numbers must be non-negative");
        }
        if (payloadLength < 0 || payloadLength > BinaryPacketCodec.MAX_PAYLOAD_BYTES) {
            throw new IllegalArgumentException("invalid payload length: " + payloadLength);
        }
    }

    /**
     * Returns a copy with the supplied checksum.
     *
     * @param computedChecksum computed checksum
     * @return header containing the checksum
     */
    public PacketHeader withChecksum(int computedChecksum) {
        return new PacketHeader(
                version,
                type,
                flags,
                sessionId,
                sequenceNumber,
                acknowledgementNumber,
                payloadLength,
                computedChecksum);
    }
}
