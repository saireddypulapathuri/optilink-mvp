package com.optilink.decoder;

import com.optilink.optical.OpticalSymbol;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.protocol.OpticalPacket;
import java.util.Objects;

/**
 * Decodes captured optical symbols into verified protocol packets.
 */
public final class PacketSymbolDecoder {
    private final BinaryPacketCodec packetCodec;

    /**
     * Creates a symbol decoder.
     *
     * @param packetCodec binary packet codec
     */
    public PacketSymbolDecoder(BinaryPacketCodec packetCodec) {
        this.packetCodec = Objects.requireNonNull(packetCodec, "packetCodec");
    }

    /**
     * Decodes and verifies an optical symbol.
     *
     * @param symbol captured optical symbol
     * @return decoded protocol packet
     */
    public OpticalPacket decode(OpticalSymbol symbol) {
        return packetCodec.decode(Objects.requireNonNull(symbol, "symbol").frameBytes());
    }
}
