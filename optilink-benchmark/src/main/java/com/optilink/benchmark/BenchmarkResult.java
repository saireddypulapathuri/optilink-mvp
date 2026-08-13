package com.optilink.benchmark;

import java.time.Duration;
import java.util.Objects;

/**
 * Measurement emitted by the lightweight benchmark harness.
 *
 * @param name benchmark name
 * @param iterations completed iterations
 * @param elapsed elapsed benchmark time
 */
public record BenchmarkResult(String name, long iterations, Duration elapsed) {

    /**
     * Creates a benchmark result.
     */
    public BenchmarkResult {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(elapsed, "elapsed");
        if (iterations < 1) {
            throw new IllegalArgumentException("iterations must be positive");
        }
    }

    /**
     * Returns operations per second.
     *
     * @return throughput
     */
    public double operationsPerSecond() {
        return iterations / (elapsed.toNanos() / 1_000_000_000.0);
    }
}
