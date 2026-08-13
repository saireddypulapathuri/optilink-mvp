# OptiLink Security

## Architecture Overview

`optilink-security` implements cryptographic primitives and replay protection used by sessions and packet protection.

## Design Goals

- Use platform-reviewed JCA/JCE primitives.
- Avoid hardcoded secrets and custom cryptography.
- Keep nonce, key derivation, and replay behavior explicit.

## Diagrams

Security sequence and threat diagrams are in `../optilink-documentation/SECURITY.md`.

## Protocol Specification and Packet Format

This module does not define packet fields. It supplies keys, signatures, AEAD, nonces, and replay checks consumed by protocol/session code.

## State Machine

`ReplayWindow` and `NonceSequence` maintain minimal synchronized state. Session lifecycle is in `optilink-session`.

## API Documentation

Public API includes Ed25519 identity service, X25519 agreement, HKDF-SHA-256, AES-GCM packet cipher, session keys, nonce sequence, replay window, and passphrase sealed-payload support.

## Security Model and Threat Model

See `../optilink-documentation/SECURITY.md`. The stack implements forward secrecy primitives, AEAD, replay defense, transcript-bound directional keys, and passphrase-sealed MVP payloads.

## Performance Model and Benchmarks

Security benchmarks are planned. Current tests validate RFC 5869 HKDF vectors and AEAD tamper rejection.

## Deployment Guide

Deploy with a FIPS-appropriate provider where required by enterprise policy. Persist identity private keys in platform secure storage in later phases.

## Developer Guide

Never add unauthenticated encryption modes. Treat byte arrays as sensitive and copy at boundaries.

## Contribution Guide

Security changes require negative tests and documentation updates.

## Extension Guide

ChaCha20-Poly1305 can be added behind an AEAD strategy interface when provider and platform requirements are confirmed.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The module uses Java platform providers for production safety. Identity enrollment, hardware keystore integration, and formal handshake serialization remain future work.
