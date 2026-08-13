package com.optilink.protocol;

import com.optilink.core.BinaryPayload;
import java.util.Objects;

/**
 * Complete protocol packet before optical symbol rendering.
 *
 * @param header packet header
 * @param payload packet payload
 */
public record OpticalPacket(PacketHeader header, BinaryPayload payload) {

    /**
     * Creates a packet and verifies header and payload lengths match.
     */
    public OpticalPacket {
        Objects.requireNonNull(header, "header");
        Objects.requireNonNull(payload, "payload");
        if (header.payloadLength() != payload.length()) {
            throw new IllegalArgumentException("header payload length does not match payload");
        }
    }
}
