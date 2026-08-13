# OptiLink Desktop

## Architecture Overview

`optilink-desktop` is the JavaFX desktop adapter boundary.

## Design Goals

- Keep UI code out of protocol modules.
- Prepare for display rendering and camera capture integration.
- Allow desktop-specific diagnostics.

## Diagrams

Desktop is an adapter in `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

Desktop consumes API and optical symbols, not raw protocol internals.

## State Machine

Future JavaFX controllers will observe session state from `optilink-session`.

## API Documentation

Public API: `DesktopBootstrap`.

## Security Model and Threat Model

Desktop UI must surface identity verification and never display decrypted data before authentication.

## Performance Model and Benchmarks

Future desktop benchmarks will measure render FPS and camera decode success.

## Deployment Guide

Package as a platform application after JavaFX dependencies are added.

## Developer Guide

Keep JavaFX classes inside this module.

## Contribution Guide

UI changes must include usability and security review notes.

## Extension Guide

Add display renderer, camera adapter, and diagnostics panels in later phases.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

Phase 1 includes only a bootstrap boundary.
