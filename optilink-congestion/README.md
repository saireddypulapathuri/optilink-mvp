# OptiLink Congestion Control

## Architecture Overview

`optilink-congestion` controls sender window size using optical-channel feedback.

## Design Goals

- Keep the controller small and replaceable.
- React to packet loss, low decode ratio, and high latency.
- Avoid coupling to packet encoding or camera implementations.

## Diagrams

Congestion is shown in the architecture flow in `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

No wire format is defined here. Transport converts ACK and channel feedback into controller calls.

## State Machine

The Phase 1 controller uses additive increase and multiplicative decrease.

## API Documentation

Public API: `CongestionController`, `AdaptiveWindowController`, `ChannelFeedback`.

## Security Model and Threat Model

Congestion inputs from peers are untrusted and should be bounded by transport policy. Phase 1 feedback values are validated.

## Performance Model and Benchmarks

Controller operations are constant time. Hardware-loop benchmarks will tune thresholds.

## Deployment Guide

Use conservative windows for first device pairings.

## Developer Guide

Add new algorithms behind the `CongestionController` interface.

## Contribution Guide

Include tests for growth, loss, and boundary behavior.

## Extension Guide

Delay-based, BBR-like, or device-profile-specific controllers can be added later.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

AIMD is simple and predictable but not optimal. More advanced control needs measured optical traces.
