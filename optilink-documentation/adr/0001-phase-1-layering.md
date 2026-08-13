# ADR 0001: Phase 1 Layering

## Status

Accepted.

## Context

OptiLink needs to become a complete optical communication stack, but the physical display-camera channel will evolve as measurements arrive. Locking transport, security, and session logic to a QR or OpenCV implementation would make the protocol hard to maintain and test.

## Decision

Implement Phase 1 as a layered Java 21 Maven reactor. Keep cryptography, protocol framing, reliable transport, congestion control, optical profile selection, and platform adapters in separate modules.

## Consequences

- Domain modules are testable without cameras or displays.
- Adapters can be replaced as ZXing, OpenCV, JavaFX, and Android implementations mature.
- Some module boundaries add ceremony in Phase 1, but they prevent cross-layer coupling in later phases.

## Alternatives Considered

- Single-module prototype: rejected because it would hide coupling and make platform ports difficult.
- QR-first app: rejected because OptiLink is a protocol stack, not a QR generator.
- Physical-layer-first research implementation: rejected because secure reliable application semantics must be stable before optimizing hardware-specific symbol schemes.
