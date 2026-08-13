# Benchmarks

## Baseline

The current lightweight benchmarks measure binary protocol encode/decode throughput, QR PNG render/decode throughput, and encrypted MVP transfer throughput.

```text
protocol-codec iterations=10000 opsPerSecond=190197.23
qr-render-decode iterations=200 opsPerSecond=33.88
mvp-transfer iterations=5 opsPerSecond=3.35
```

## Method

The protocol benchmark runs `BinaryPacketCodec.encode` followed by `BinaryPacketCodec.decode` for 10,000 iterations using a fixed payload and reports operations per second.

The QR benchmark renders an `OpticalSymbol` to a PNG QR frame and decodes the PNG back into symbol bytes for 200 iterations. Both benchmarks are deterministic smoke benchmarks and useful as regression guards, not replacements for JMH.

The MVP benchmark generates encrypted QR transfer frames, decodes those frames, reassembles the payload, and decrypts the recovered data. The current MVP uses a conservative QR payload budget to favor decode reliability over raw frame density.

## Interpretation

The result shows the packet codec is not the expected bottleneck. QR rendering and decode are substantially slower and real-world throughput will additionally be dominated by display refresh rate, camera exposure, blur, perspective correction, and decoder success ratio.

## Future Benchmark Work

- Replace the lightweight harness with JMH for stable JVM warmup and allocation profiling.
- Track OpenCV preprocessing, QR decode time, transport recovery latency, CPU usage, and memory usage.
- Store benchmark history for every performance-affecting change.
