package com.optilink.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.optilink.congestion.AdaptiveWindowController;
import com.optilink.core.BinaryPayload;
import com.optilink.protocol.SelectiveAckFrame;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReliableTransportSenderTest {

    @Test
    void chunksPayloadAndHonorsWindow() {
        ReliableTransportSender sender = sender(2);
        sender.enqueue(new BinaryPayload(new byte[] {1, 2, 3, 4, 5}));

        var firstBatch = sender.pollReady(Instant.EPOCH);
        sender.onAcknowledgement(new SelectiveAckFrame(1, List.of()));
        var secondBatch = sender.pollReady(Instant.EPOCH.plusMillis(1));

        assertEquals(2, firstBatch.size());
        assertEquals(1, secondBatch.size());
        assertFalse(sender.isIdle());
    }

    @Test
    void retransmitsExpiredPackets() {
        ReliableTransportSender sender = sender(4);
        sender.enqueue(new BinaryPayload(new byte[] {1, 2, 3}));

        sender.pollReady(Instant.EPOCH);
        var retransmitted = sender.pollReady(Instant.EPOCH.plusMillis(500));

        assertEquals(1, retransmitted.size());
        assertEquals(1, retransmitted.getFirst().header().sequenceNumber());
    }

    private ReliableTransportSender sender(int maxPayloadBytes) {
        return new ReliableTransportSender(
                UUID.randomUUID(),
                new AdaptiveWindowController(1, 2, 4),
                new TransportParameters(maxPayloadBytes, Duration.ofMillis(250)));
    }
}
