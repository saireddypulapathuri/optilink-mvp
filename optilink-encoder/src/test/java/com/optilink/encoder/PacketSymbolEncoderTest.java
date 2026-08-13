package com.optilink.encoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.optilink.core.BinaryPayload;
import com.optilink.optical.ErrorCorrectionLevel;
import com.optilink.optical.OpticalProfile;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.protocol.OpticalPacket;
import com.optilink.protocol.PacketHeader;
import com.optilink.protocol.PacketType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PacketSymbolEncoderTest {

    @Test
    void bindsPacketBytesToOpticalProfile() {
        BinaryPayload payload = new BinaryPayload(new byte[] {1, 2});
        OpticalPacket packet = packet(payload);
        var profile = new OpticalProfile("test", 10, 8, ErrorCorrectionLevel.Q, 80, 10);
        PacketSymbolEncoder encoder = new PacketSymbolEncoder(new BinaryPacketCodec());

        var symbol = encoder.encode(packet, profile);

        assertEquals(profile, symbol.profile());
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
