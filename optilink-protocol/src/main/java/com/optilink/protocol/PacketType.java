package com.optilink.protocol;

/**
 * Wire-visible packet roles in the OptiLink protocol.
 */
public enum PacketType {
    /**
     * Handshake material used before application data is accepted.
     */
    HANDSHAKE(1),

    /**
     * Encrypted application data.
     */
    DATA(2),

    /**
     * Selective acknowledgement payload.
     */
    ACK(3),

    /**
     * Key rotation control packet.
     */
    KEY_UPDATE(4),

    /**
     * Graceful session shutdown packet.
     */
    CLOSE(5);

    private final int wireValue;

    PacketType(int wireValue) {
        this.wireValue = wireValue;
    }

    /**
     * Returns the one-byte wire value.
     *
     * @return wire value
     */
    public int wireValue() {
        return wireValue;
    }

    /**
     * Resolves a packet type from its wire value.
     *
     * @param wireValue encoded packet type
     * @return matching packet type
     */
    public static PacketType fromWireValue(int wireValue) {
        for (PacketType type : values()) {
            if (type.wireValue == wireValue) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown packet type: " + wireValue);
    }
}
