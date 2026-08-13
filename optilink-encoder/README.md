# OptiLink Encoder

## Architecture Overview

`optilink-encoder` transforms verified protocol packets into optical symbols and renders QR-compatible PNG frames.

## Design Goals

- Keep encoding profile-aware.
- Reject packets that exceed profile capacity.
- Keep QR rendering behind a focused adapter class.

## Diagrams

Encoder flow is shown in `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

Uses `BinaryPacketCodec` from `optilink-protocol`.

## State Machine

The encoder is stateless.

## API Documentation

Public API: `PacketSymbolEncoder`, `QrSymbolRenderer`.

## Security Model and Threat Model

The encoder must not modify encrypted/authenticated packet bytes.

## Performance Model and Benchmarks

Binary symbol encoding is cheap. QR PNG render/decode benchmarks are recorded in `../optilink-documentation/BENCHMARKS.md`.

## Deployment Guide

Use with an optical profile selected from live channel metrics.

## Developer Guide

Renderer-specific code belongs in focused renderer classes and must not leak into protocol framing.

## Contribution Guide

Capacity checks must remain strict.

## Extension Guide

Add multi-QR or RGB renderers behind separate interfaces.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The module now renders QR PNG frames with ZXing. Display refresh scheduling and hardware presentation remain future work.
