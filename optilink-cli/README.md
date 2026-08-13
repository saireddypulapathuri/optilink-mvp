# OptiLink CLI

## Architecture Overview

`optilink-cli` provides command-line smoke tests, QR PNG output, practical encrypted MVP transfers, and future operator tools.

## Design Goals

- Exercise protocol and encoder wiring from a simple entry point.
- Avoid embedding business logic in CLI parsing.
- Keep output safe and non-secret.

## Diagrams

CLI is an adapter in `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

CLI uses protocol, security, encoder, and decoder modules to produce optical symbols, QR PNG frames, and passphrase-sealed QR transfer directories.

## State Machine

The current CLI is stateless.

Example:

```bash
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args='--qr /tmp/optilink.png hello'
```

MVP transfer:

```bash
export OPTILINK_PASSPHRASE='choose a strong shared passphrase'
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args='send --out /tmp/optilink-transfer --text hello'
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args='receive --in /tmp/optilink-transfer'
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args='inspect --in /tmp/optilink-transfer'
```

## API Documentation

Public API: `OptiLinkCli`.

## Security Model and Threat Model

CLI must never print private keys or plaintext decrypted from untrusted peers.

## Performance Model and Benchmarks

CLI is not a benchmark harness. Use `optilink-benchmark`.

## Deployment Guide

Package as an executable jar in a future release.

## Developer Guide

Keep commands thin and delegate to services.

## Contribution Guide

Add tests for command behavior when CLI parsing is expanded.

## Extension Guide

Future commands can include key generation, benchmark invocation, and diagnostic capture.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The CLI can produce QR PNG frames, run passphrase-sealed MVP transfers, and inspect transfer completeness. Live camera capture remains future work.
