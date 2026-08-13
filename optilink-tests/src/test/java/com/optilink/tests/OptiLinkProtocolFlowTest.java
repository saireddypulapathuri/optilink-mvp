package com.optilink.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.optilink.congestion.AdaptiveWindowController;
import com.optilink.core.BinaryPayload;
import com.optilink.decoder.PacketSymbolDecoder;
import com.optilink.decoder.QrSymbolImageDecoder;
import com.optilink.encoder.PacketSymbolEncoder;
import com.optilink.encoder.QrSymbolRenderer;
import com.optilink.optical.AdaptiveOpticalProfileSelector;
import com.optilink.optical.OpticalChannelMetrics;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.transport.ReliableTransportReceiver;
import com.optilink.transport.ReliableTransportSender;
import com.optilink.transport.TransportParameters;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OptiLinkProtocolFlowTest {

    @Test
    void payloadTraversesTransportEncoderDecoderAndReceiver() {
        BinaryPacketCodec codec = new BinaryPacketCodec();
        var sender = new ReliableTransportSender(
                UUID.randomUUID(),
                new AdaptiveWindowController(1, 4, 8),
                new TransportParameters(512, Duration.ofMillis(250)));
        var receiver = new ReliableTransportReceiver();
        var profile = new AdaptiveOpticalProfileSelector()
                .select(new OpticalChannelMetrics(60, 0.99, 0.1, 0.8, 100));
        var encoder = new PacketSymbolEncoder(codec);
        var decoder = new PacketSymbolDecoder(codec);
        var qrRenderer = new QrSymbolRenderer();
        var qrDecoder = new QrSymbolImageDecoder();
        byte[] source = "zero trust optical message".getBytes(StandardCharsets.UTF_8);

        sender.enqueue(new BinaryPayload(source));
        var packet = sender.pollReady(Instant.EPOCH).getFirst();
        var symbol = encoder.encode(packet, profile);
        var decodedSymbol = qrDecoder.decode(qrRenderer.render(symbol));
        var decoded = decoder.decode(decodedSymbol);
        var delivered = receiver.receive(decoded);

        assertEquals(1, delivered.size());
        assertArrayEquals(source, delivered.getFirst().bytes());
    }
}
