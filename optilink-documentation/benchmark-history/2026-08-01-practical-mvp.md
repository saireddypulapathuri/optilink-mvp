# 2026-08-01 Practical MVP Benchmark

## Environment

- Workspace: `/Users/psr/Documents/Opt`
- Java runtime available during verification: Java 25 compiling Java 21 release bytecode
- Maven: 3.9.11
- QR library: ZXing 3.5.4

## Commands

```bash
mvn -q -f optilink-benchmark/pom.xml exec:java -Dexec.mainClass=com.optilink.benchmark.ProtocolBenchmark
mvn -q -f optilink-benchmark/pom.xml exec:java -Dexec.mainClass=com.optilink.benchmark.QrRenderDecodeBenchmark
mvn -q -f optilink-benchmark/pom.xml exec:java -Dexec.mainClass=com.optilink.benchmark.MvpTransferBenchmark
```

## Results

```text
protocol-codec iterations=10000 opsPerSecond=190197.23
qr-render-decode iterations=200 opsPerSecond=33.88
mvp-transfer iterations=5 opsPerSecond=3.35
```

## Notes

The MVP transfer benchmark includes passphrase sealing with PBKDF2-HMAC-SHA256, QR frame generation, QR image decode, transfer reassembly, and AES-GCM authentication/decryption. The current MVP uses a conservative QR payload budget to favor decode reliability over raw frame density. It still does not include live display refresh, camera exposure, motion blur, perspective correction, or automatic missing-frame recovery.
