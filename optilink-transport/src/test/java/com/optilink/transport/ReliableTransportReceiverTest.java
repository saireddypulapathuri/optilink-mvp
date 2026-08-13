package com.optilink.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.optilink.core.BinaryPayload;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.protocol.OpticalPacket;
import com.optilink.protocol.PacketHeader;
import com.optilink.protocol.PacketType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReliableTransportReceiverTest {

    @Test
    void buffersOutOfOrderAndAcknowledgesRanges() {
        ReliableTransportReceiver receiver = new ReliableTransportReceiver();
        UUID sessionId = UUID.randomUUID();

        assertTrue(receiver.receive(packet(sessionId, 2, 2)).isEmpty());
        assertEquals(1, receiver.acknowledgement().ranges().size());
        assertEquals(2, receiver.receive(packet(sessionId, 1, 1)).size());
        assertEquals(2, receiver.acknowledgement().cumulativeAck());
    }

    private OpticalPacket packet(UUID sessionId, long sequence, int value) {
        BinaryPayload payload = new BinaryPayload(new byte[] {(byte) value});
        PacketHeader header = new PacketHeader(
                BinaryPacketCodec.CURRENT_VERSION,
                PacketType.DATA,
                0,
                sessionId,
                sequence,
                0,
                payload.length(),
                0);
        return new OpticalPacket(header, payload);
    }
}
