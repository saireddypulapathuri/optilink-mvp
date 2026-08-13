package com.optilink.encoder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.optilink.core.BinaryPayload;
import com.optilink.optical.ErrorCorrectionLevel;
import com.optilink.optical.OpticalProfile;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.protocol.OpticalPacket;
import com.optilink.protocol.PacketHeader;
import com.optilink.protocol.PacketType;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class QrSymbolRendererTest {

    @Test
    void rendersReadablePngQrFrame() throws Exception {
        BinaryPacketCodec codec = new BinaryPacketCodec();
        BinaryPayload payload = new BinaryPayload(new byte[] {1, 2, 3});
        OpticalPacket packet = packet(payload);
        OpticalProfile profile = new OpticalProfile("qr", 10, 10, ErrorCorrectionLevel.Q, 80, 64);
        var symbol = new PacketSymbolEncoder(codec).encode(packet, profile);

        var frame = new QrSymbolRenderer().render(symbol);
        var image = ImageIO.read(new ByteArrayInputStream(frame.imageBytes()));

        assertEquals("image/png", frame.mimeType());
        assertEquals(frame.width(), image.getWidth());
        assertEquals(frame.height(), image.getHeight());
        assertTrue(frame.imageBytes().length > 100);
        assertArrayEquals(symbol.frameBytes(), codec.encode(packet));
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
