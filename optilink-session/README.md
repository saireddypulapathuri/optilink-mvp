# OptiLink Session

## Architecture Overview

`optilink-session` owns secure session lifecycle, policy, transcript hashing, and session construction.

## Design Goals

- Bind session keys to authenticated transcript context.
- Keep long-lived identity decisions separate from transport.
- Make key rotation policy explicit.

## Diagrams

Session state diagrams are in `../optilink-documentation/ARCHITECTURE.md` and `../optilink-documentation/PROTOCOL.md`.

## Protocol Specification and Packet Format

Session consumes handshake material and produces `SecureSession`. Handshake packet serialization is planned after the adapter loop is implemented.

## State Machine

Session states: `NEW`, `HANDSHAKING`, `ESTABLISHED`, `REKEYING`, `CLOSED`, `FAILED`.

## API Documentation

Public API includes `SessionState`, `SessionPolicy`, `SessionDescriptor`, `SecureSession`, `HandshakeTranscript`, `SessionEstablishmentContext`, and `SecureSessionFactory`.

## Security Model and Threat Model

Session keys are derived through HKDF from X25519 shared secrets and transcript hashes. Peer signature validation must occur before `SecureSessionFactory.establish`.

## Performance Model and Benchmarks

Session construction is not expected to be hot-path. Future benchmarks will cover handshake latency.

## Deployment Guide

Use production default policy unless enterprise deployment defines stricter key rotation and replay windows.

## Developer Guide

Do not accept application payloads until state is `ESTABLISHED`.

## Contribution Guide

Session changes must include security review notes.

## Extension Guide

Add session resume as a new authenticated transcript mode rather than bypassing handshake checks.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

Phase 1 models establishment after authentication. Full interoperable handshake messages are planned next.
