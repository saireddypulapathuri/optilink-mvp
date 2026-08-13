# OptiLink Compression

## Architecture Overview

`optilink-compression` provides payload compression before encryption.

## Design Goals

- Reduce optical airtime for compressible payloads.
- Keep compression optional and strategy-based.
- Use runtime-provided algorithms first.

## Diagrams

Compression sits before transport in `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

Compression does not define packet format. A future content-type flag must describe compressed payloads.

## State Machine

The compressor is stateless per call.

## API Documentation

Public API: `PayloadCompressor`, `DeflatePayloadCompressor`.

## Security Model and Threat Model

Compression before encryption can leak information when attacker-controlled plaintext is mixed with secrets. Avoid that pattern.

## Performance Model and Benchmarks

Compression trades CPU for fewer optical bytes. Benchmarks are planned by payload type.

## Deployment Guide

Enable only for payload classes that benefit from compression.

## Developer Guide

Keep decompression limits in mind when adding streaming compression.

## Contribution Guide

Round-trip tests are required for each compressor.

## Extension Guide

Add Brotli/Zstandard adapters only when deployment dependencies are approved.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

Deflate is portable but not always optimal. Adaptive compression selection is future work.
