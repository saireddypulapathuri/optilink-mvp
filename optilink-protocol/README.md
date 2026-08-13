# OptiLink Protocol

## Architecture Overview

`optilink-protocol` defines the canonical packet wire image, selective acknowledgement frames, and MVP transfer chunk payloads.

## Design Goals

- Keep framing deterministic and versioned.
- Reject corrupted frames before higher-cost processing.
- Keep packet encoding independent from optical rendering.

## Diagrams

Protocol flow and state diagrams are in `../optilink-documentation/PROTOCOL.md` and `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

The binary packet header is 48 bytes and includes magic, version, type, flags, session ID, sequence, acknowledgement, payload length, and CRC32C. See `../optilink-documentation/PROTOCOL.md`.

## State Machine

Protocol packets are stateless values. Session and transport modules own state transitions.

## API Documentation

Public API: `BinaryPacketCodec`, `OpticalPacket`, `PacketHeader`, `PacketType`, `SelectiveAckCodec`, `SelectiveAckFrame`, `AckRange`, `TransferChunk`, `TransferChunkCodec`, `TransferChunker`, `TransferReassembler`, `PacketIntegrityException`.

## Security Model and Threat Model

CRC32C is corruption detection only. Authentication is provided by `optilink-security`.

## Performance Model and Benchmarks

Baseline codec throughput is recorded in `../optilink-documentation/BENCHMARKS.md`.

## Deployment Guide

Use this module from transport, encoder, decoder, and tests.

## Developer Guide

Any wire-format change requires protocol documentation, tests, and versioning review.

## Contribution Guide

Never add platform rendering or cryptographic policy here.

## Extension Guide

Add new packet types through `PacketType` and document compatibility behavior.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The Phase 1 header is intentionally explicit and slightly larger than a packed bitfield to preserve debuggability. Future work can add compact encrypted header variants after interoperability tests exist.
