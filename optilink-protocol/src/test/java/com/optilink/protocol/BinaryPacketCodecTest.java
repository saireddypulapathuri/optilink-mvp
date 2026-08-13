package com.optilink.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.optilink.core.BinaryPayload;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BinaryPacketCodecTest {

    @Test
    void packetRoundTripsWithComputedChecksum() {
        BinaryPacketCodec codec = new BinaryPacketCodec();
        byte[] payload = "secure optical payload".getBytes(StandardCharsets.UTF_8);
        PacketHeader header = new PacketHeader(
                BinaryPacketCodec.CURRENT_VERSION,
                PacketType.DATA,
                0,
                UUID.randomUUID(),
                7,
                6,
                payload.length,
                0);

        OpticalPacket decoded = codec.decode(codec.encode(new OpticalPacket(header, new BinaryPayload(payload))));

        assertEquals(PacketType.DATA, decoded.header().type());
        assertEquals(7, decoded.header().sequenceNumber());
        assertArrayEquals(payload, decoded.payload().bytes());
    }

    @Test
    void decoderRejectsCorruptedPayload() {
        BinaryPacketCodec codec = new BinaryPacketCodec();
        byte[] payload = {1, 2, 3};
        PacketHeader header = new PacketHeader(
                BinaryPacketCodec.CURRENT_VERSION,
                PacketType.DATA,
                0,
                UUID.randomUUID(),
                1,
                0,
                payload.length,
                0);
        byte[] encoded = codec.encode(new OpticalPacket(header, new BinaryPayload(payload)));

        encoded[encoded.length - 1] ^= 0x01;

        assertThrows(PacketIntegrityException.class, () -> codec.decode(encoded));
    }
}
