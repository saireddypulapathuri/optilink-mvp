# OptiLink Decoder

## Architecture Overview

`optilink-decoder` turns captured optical symbols and QR PNG frames back into protocol packets.

## Design Goals

- Reuse canonical protocol decoding.
- Fail fast on corrupt frames.
- Keep QR image decoding outside the protocol packet decoder.

## Diagrams

Decoder flow is shown in `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

Uses `BinaryPacketCodec` from `optilink-protocol`.

## State Machine

The decoder is stateless.

## API Documentation

Public API: `PacketSymbolDecoder`, `QrSymbolImageDecoder`.

## Security Model and Threat Model

Decoded packets still require AEAD authentication and replay checks before application delivery.

## Performance Model and Benchmarks

Protocol and QR decode baselines are in `../optilink-documentation/BENCHMARKS.md`. Real camera decoding benchmarks are future work.

## Deployment Guide

Use after camera calibration and symbol extraction, or directly with rendered PNG frames in tests/tools.

## Developer Guide

Keep OpenCV preprocessing separate from QR image decoding.

## Contribution Guide

Add corruption and malformed input tests for every decoder path.

## Extension Guide

Future decoders can support multi-symbol reassembly and soft-decision metadata.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The module decodes QR PNG frames and symbol bytes. Raw camera-frame extraction remains future work.
