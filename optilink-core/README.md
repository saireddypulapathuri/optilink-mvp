# OptiLink Core

## Architecture Overview

`optilink-core` contains shared immutable values and base exceptions. It has no dependency on protocol, transport, security, UI, or server modules.

## Design Goals

- Keep common types small, immutable, and dependency-light.
- Prevent accidental byte-array mutation at trust boundaries.
- Provide secure entropy through an injectable port.

## Diagrams

Component, package, sequence, state, class, and flow diagrams are maintained in `../optilink-documentation/ARCHITECTURE.md`. This module sits at the bottom of the dependency graph.

## Protocol Specification and Packet Format

Core does not define wire formats. It supplies `BinaryPayload` and `DeviceId` values used by protocol and session modules.

## State Machine

Core is stateless except for `SecureEntropySource`, which delegates to `SecureRandom`.

## API Documentation

Public API: `BinaryPayload`, `DeviceId`, `EntropySource`, `SecureEntropySource`, `OptiLinkException`. Javadocs are part of the source.

## Security Model and Threat Model

Mutable byte arrays are defensively copied. Entropy consumers depend on `EntropySource` so tests can inject deterministic entropy and production can use `SecureRandom`.

## Performance Model and Benchmarks

Payload copies are intentional safety costs at module boundaries. Benchmarking belongs in `optilink-benchmark`.

## Deployment Guide

This module is a library dependency only.

## Developer Guide

Keep this module free of framework dependencies.

## Contribution Guide

Add only cross-cutting primitives here. Domain-specific types belong in their owning module.

## Extension Guide

Add new ports as interfaces and keep implementations in adapter modules.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The current design favors safety over zero-copy performance. Future work may add explicit pooled-buffer abstractions if profiling shows copies are a bottleneck.
