# OptiLink Transport

## Architecture Overview

`optilink-transport` provides reliable delivery over lossy optical packets.

## Design Goals

- Sliding-window scheduling.
- Selective ACK processing.
- Timeout and fast retransmit recovery.
- Ordered delivery and duplicate suppression.

## Diagrams

Transport flow diagrams are in `../optilink-documentation/PROTOCOL.md`.

## Protocol Specification and Packet Format

Transport uses `DATA` packets and `SelectiveAckFrame` payloads defined in `optilink-protocol`.

## State Machine

Sender state is backlog plus in-flight map. Receiver state is next expected sequence plus out-of-order buffer.

## API Documentation

Public API: `TransportParameters`, `PendingTransmission`, `ReliableTransportSender`, `ReliableTransportReceiver`.

## Security Model and Threat Model

Transport sequence numbers support replay validation, but cryptographic replay enforcement belongs in `optilink-security`.

## Performance Model and Benchmarks

Window size bounds memory and camera load. Future benchmarks will measure throughput, latency, and recovery time under synthetic loss.

## Deployment Guide

Tune payload size and timeout from optical profile feedback.

## Developer Guide

Keep transport independent from QR, OpenCV, and UI adapters.

## Contribution Guide

Tests must cover loss, duplicate ACKs, out-of-order delivery, and timeout behavior.

## Extension Guide

Add flow control, stream multiplexing, and session resume as explicit protocol extensions.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

Phase 1 implements packet reliability, not multi-stream QUIC-style transport. Stream support is future work.
