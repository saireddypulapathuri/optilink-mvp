package com.optilink.decoder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.optilink.core.BinaryPayload;
import com.optilink.optical.ErrorCorrectionLevel;
import com.optilink.optical.OpticalProfile;
import com.optilink.optical.OpticalSymbol;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.protocol.OpticalPacket;
import com.optilink.protocol.PacketHeader;
import com.optilink.protocol.PacketType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PacketSymbolDecoderTest {

    @Test
    void decodesSymbolBackToPacket() {
        BinaryPacketCodec codec = new BinaryPacketCodec();
        BinaryPayload payload = new BinaryPayload(new byte[] {7, 8});
        OpticalPacket packet = packet(payload);
        var profile = new OpticalProfile("test", 10, 8, ErrorCorrectionLevel.Q, 80, 10);
        var symbol = new OpticalSymbol(profile, codec.encode(packet));

        OpticalPacket decoded = new PacketSymbolDecoder(codec).decode(symbol);

        assertArrayEquals(payload.bytes(), decoded.payload().bytes());
    }

    private OpticalPacket packet(BinaryPayload payload) {
        PacketHeader header = new PacketHeader(
                BinaryPacketCodec.CURRENT_VERSION,
                PacketType.DATA,
                0,
                UUID.randomUUID(),
                1,
                0,
                payload.length(),
                0);
        return new OpticalPacket(header, payload);
    }
}
