package com.optilink.encoder;

import com.optilink.optical.OpticalProfile;
import com.optilink.optical.OpticalSymbol;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.protocol.OpticalPacket;
import java.util.Objects;

/**
 * Encodes protocol packets into optical symbols.
 */
public final class PacketSymbolEncoder {
    private final BinaryPacketCodec packetCodec;

    /**
     * Creates a symbol encoder.
     *
     * @param packetCodec binary packet codec
     */
    public PacketSymbolEncoder(BinaryPacketCodec packetCodec) {
        this.packetCodec = Objects.requireNonNull(packetCodec, "packetCodec");
    }

    /**
     * Encodes a packet into a profile-bound optical symbol.
     *
     * @param packet packet to encode
     * @param profile selected optical profile
     * @return encoded optical symbol
     */
    public OpticalSymbol encode(OpticalPacket packet, OpticalProfile profile) {
        Objects.requireNonNull(profile, "profile");
        byte[] frameBytes = packetCodec.encode(Objects.requireNonNull(packet, "packet"));
        if (packet.payload().length() > profile.maxPayloadBytes()) {
            throw new IllegalArgumentException("packet payload exceeds optical profile capacity");
        }
        return new OpticalSymbol(profile, frameBytes);
    }
}
