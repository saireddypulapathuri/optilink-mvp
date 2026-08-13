# 2026-08-01 QR Carrier Benchmark

## Environment

- Workspace: `/Users/psr/Documents/Opt`
- Java runtime available during verification: Java 25 compiling Java 21 release bytecode
- Maven: 3.9.11
- QR library: ZXing 3.5.4

## Commands

```bash
mvn -q -f optilink-benchmark/pom.xml exec:java -Dexec.mainClass=com.optilink.benchmark.ProtocolBenchmark
mvn -q -f optilink-benchmark/pom.xml exec:java -Dexec.mainClass=com.optilink.benchmark.QrRenderDecodeBenchmark
```

## Results

```text
protocol-codec iterations=10000 opsPerSecond=125328.99
qr-render-decode iterations=200 opsPerSecond=28.62
```

## Notes

The QR benchmark includes PNG rendering and ZXing image decode but still does not include display refresh, real camera capture, exposure changes, perspective correction, or motion blur.
