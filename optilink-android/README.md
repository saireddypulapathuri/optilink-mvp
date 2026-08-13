# OptiLink Android

## Architecture Overview

`optilink-android` is the Android adapter contract boundary for the future Kotlin implementation.

## Design Goals

- Keep mobile UI and camera APIs separate from protocol code.
- Prepare for Kotlin integration without forcing Android plugins into the Java reactor.
- Preserve shared domain modules for reuse.

## Diagrams

Android is an adapter in `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

Android should use the shared protocol and optical modules through adapter facades.

## State Machine

Future Android UI follows session states from `optilink-session`.

## API Documentation

Public API: `AndroidAdapterContract`.

## Security Model and Threat Model

Identity keys should be stored in Android Keystore when implemented.

## Performance Model and Benchmarks

Future mobile benchmarks will measure camera FPS, thermal impact, CPU usage, and decode success.

## Deployment Guide

Android Gradle integration is future work.

## Developer Guide

Keep Kotlin and Android SDK dependencies inside this module.

## Contribution Guide

Changes must preserve compatibility with the shared Java domain modules.

## Extension Guide

Add CameraX, screen brightness control, and secure storage adapters later.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

Phase 1 keeps Android as a contract to avoid mixing Maven and Android Gradle complexity prematurely.
