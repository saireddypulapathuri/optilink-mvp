# OptiLink Tests

## Architecture Overview

`optilink-tests` contains cross-module integration tests.

## Design Goals

- Verify that independently tested modules compose correctly.
- Keep end-to-end protocol behavior visible.
- Avoid hardware dependencies in Phase 1 CI.

## Diagrams

Integration flow is documented in `../optilink-documentation/TESTING.md`.

## Protocol Specification and Packet Format

Tests exercise the protocol through transport, QR rendering, QR decoding, symbol decoding, and receiver modules.

## State Machine

Integration tests cover transport sender/receiver behavior and QR carrier round trips.

## API Documentation

This module has test classes only.

## Security Model and Threat Model

Future integration tests will include authenticated encryption and replay rejection across full flows.

## Performance Model and Benchmarks

Performance tests live in `optilink-benchmark`.

## Deployment Guide

Run with the full reactor: `mvn test`.

## Developer Guide

Keep tests deterministic and independent from local camera hardware.

## Contribution Guide

Add a cross-module test whenever a public contract spans modules.

## Extension Guide

Add hardware-loop and Testcontainers suites in later phases.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The current integration suite validates byte-symbol flow through QR PNG frames. Real optical capture remains future work.
