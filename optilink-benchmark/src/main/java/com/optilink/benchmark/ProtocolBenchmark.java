package com.optilink.benchmark;

import com.optilink.core.BinaryPayload;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.protocol.OpticalPacket;
import com.optilink.protocol.PacketHeader;
import com.optilink.protocol.PacketType;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight deterministic benchmark for protocol encode/decode throughput.
 */
public final class ProtocolBenchmark {
    private static final int ITERATIONS = 10_000;

    private ProtocolBenchmark() {
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
     * Runs the protocol codec benchmark.
     *
     * @return benchmark result
     */
    public static BenchmarkResult run() {
        BinaryPacketCodec codec = new BinaryPacketCodec();
        OpticalPacket packet = packet();
        Instant start = Instant.now();
        for (int index = 0; index < ITERATIONS; index++) {
            codec.decode(codec.encode(packet));
        }
        return new BenchmarkResult("protocol-codec", ITERATIONS, Duration.between(start, Instant.now()));
    }

    private static OpticalPacket packet() {
        byte[] data = new byte[512];
        BinaryPayload payload = new BinaryPayload(data);
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
