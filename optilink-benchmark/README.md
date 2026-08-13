# OptiLink Benchmark

## Architecture Overview

`optilink-benchmark` contains repeatable benchmark harnesses.

## Design Goals

- Store performance checks outside production modules.
- Make benchmark results easy to record.
- Start with deterministic microbenchmarks before hardware tests.

## Diagrams

Benchmark flow is documented in `../optilink-documentation/BENCHMARKS.md`.

## Protocol Specification and Packet Format

The current benchmark exercises `BinaryPacketCodec`.

## State Machine

Benchmarks are stateless runs.

## API Documentation

Public API: `BenchmarkResult`, `ProtocolBenchmark`, `QrRenderDecodeBenchmark`, `MvpTransferBenchmark`.

## Security Model and Threat Model

Benchmarks must not use real secrets.

## Performance Model and Benchmarks

Baseline results: `protocol-codec iterations=10000 opsPerSecond=348468.48`; `qr-render-decode iterations=200 opsPerSecond=33.07`; `mvp-transfer iterations=5 opsPerSecond=3.61`.

## Deployment Guide

Run benchmarks locally and record results in `../optilink-documentation/benchmark-history`.

## Developer Guide

Prefer JMH for serious JVM benchmarking in later phases.

## Contribution Guide

Document hardware, JVM, command, and result for every benchmark entry.

## Extension Guide

Add OpenCV and transport-loss benchmarks.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The current harness is simple and fast but lacks JMH warmup rigor.
