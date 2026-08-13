package com.optilink.benchmark;

import com.optilink.core.BinaryPayload;
import com.optilink.decoder.QrSymbolImageDecoder;
import com.optilink.encoder.PacketSymbolEncoder;
import com.optilink.encoder.QrSymbolRenderer;
import com.optilink.optical.ErrorCorrectionLevel;
import com.optilink.optical.OpticalProfile;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.protocol.OpticalPacket;
import com.optilink.protocol.PacketHeader;
import com.optilink.protocol.PacketType;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight benchmark for QR PNG render/decode throughput.
 */
public final class QrRenderDecodeBenchmark {
    private static final int ITERATIONS = 200;

    private QrRenderDecodeBenchmark() {
    }

    /**
     * Runs the benchmark and prints results.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        BenchmarkResult result = run();
        System.out.printf("%s iterations=%d opsPerSecond=%.2f%n",
                result.name(),
                result.iterations(),
                result.operationsPerSecond());
    }

    /**
     * Runs the QR render/decode benchmark.
     *
     * @return benchmark result
     */
    public static BenchmarkResult run() {
        BinaryPacketCodec codec = new BinaryPacketCodec();
        var symbol = new PacketSymbolEncoder(codec).encode(packet(), profile());
        var renderer = new QrSymbolRenderer();
        var decoder = new QrSymbolImageDecoder();
        Instant start = Instant.now();
        for (int index = 0; index < ITERATIONS; index++) {
            decoder.decode(renderer.render(symbol));
        }
        return new BenchmarkResult("qr-render-decode", ITERATIONS, Duration.between(start, Instant.now()));
    }

    private static OpticalProfile profile() {
        return new OpticalProfile("benchmark-qr", 10, 18, ErrorCorrectionLevel.M, 85, 512);
    }

    private static OpticalPacket packet() {
        BinaryPayload payload = new BinaryPayload(new byte[64]);
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
