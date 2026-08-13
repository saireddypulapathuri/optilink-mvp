package com.optilink.benchmark;

import com.optilink.transfer.QrTransferMvpService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;

/**
 * Lightweight benchmark for encrypted QR MVP transfer generation and recovery.
 */
public final class MvpTransferBenchmark {
    private static final int ITERATIONS = 5;

    private MvpTransferBenchmark() {
    }

    /**
     * Runs the benchmark and prints results.
     *
     * @param args ignored
     * @throws Exception when benchmark setup fails
     */
    public static void main(String[] args) throws Exception {
        BenchmarkResult result = run();
        System.out.printf("%s iterations=%d opsPerSecond=%.2f%n",
                result.name(),
                result.iterations(),
                result.operationsPerSecond());
    }

    /**
     * Runs the MVP transfer benchmark.
     *
     * @return benchmark result
     * @throws Exception when benchmark setup fails
     */
    public static BenchmarkResult run() throws Exception {
        QrTransferMvpService service = new QrTransferMvpService();
        byte[] message = "benchmark encrypted optical transfer".getBytes(StandardCharsets.UTF_8);
        char[] passphrase = "correct horse battery staple".toCharArray();
        Path root = Files.createTempDirectory("optilink-mvp-benchmark");
        Instant start = Instant.now();
        try {
            for (int index = 0; index < ITERATIONS; index++) {
                Path directory = root.resolve("transfer-" + index);
                service.writeTransfer(directory, message, passphrase);
                service.readTransfer(directory, passphrase);
            }
            return new BenchmarkResult("mvp-transfer", ITERATIONS, Duration.between(start, Instant.now()));
        } finally {
            deleteRecursively(root);
        }
    }

    private static void deleteRecursively(Path root) throws Exception {
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
