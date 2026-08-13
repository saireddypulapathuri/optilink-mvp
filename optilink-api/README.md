# OptiLink API

## Architecture Overview

`optilink-api` defines application-facing ports for sessions and optical channels.

## Design Goals

- Hide protocol internals from applications.
- Keep adapters testable through stable interfaces.
- Provide a narrow entry point for future app workflows.

## Diagrams

API position is shown in `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

API callers send `BinaryPayload`; protocol details remain below the API.

## State Machine

API exposes `SessionHandle`; session state is managed by `optilink-session`.

## API Documentation

Public API: `OptiLinkEndpoint`, `OpticalChannelPort`, `SessionHandle`.

## Security Model and Threat Model

Application adapters must not bypass session establishment or packet authentication.

## Performance Model and Benchmarks

API overhead is expected to be negligible compared with optical capture and rendering.

## Deployment Guide

Adapters should depend on this module rather than domain internals where possible.

## Developer Guide

Keep methods small and command-oriented.

## Contribution Guide

Add interfaces only after a stable use case exists.

## Extension Guide

Future APIs can add receive streams, observability, and policy configuration.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

Phase 1 API is intentionally minimal. Receive-side application callbacks are planned.
