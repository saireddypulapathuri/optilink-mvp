# Roadmap

## Phase 1: Protocol Foundation

Status: complete in this scaffold.

- Multi-module Maven reactor.
- Core immutable values.
- Binary protocol and SACK codec.
- Security primitives and session model.
- Reliable sender/receiver and adaptive congestion.
- Optical profile model and encoder/decoder contract.
- CLI, server health, desktop/android/camera boundaries.
- Unit and integration tests.
- Baseline benchmark and documentation.

## Phase 2: Real Optical Carrier

- Add ZXing QR rendering and decoding adapters. Status: implemented for PNG frames.
- Add luminance frame-quality analysis for brightness, contrast, and blur. Status: implemented for grayscale frames.
- Add practical encrypted QR transfer CLI with browser display page. Status: implemented for passphrase-sealed one-way MVP.
- Add OpenCV perspective correction and calibration pipeline. Status: pending.
- Add frame synchronization markers and clock recovery. Status: pending.
- Persist identities in platform key stores.
- Add hardware-loop tests with synthetic and captured frame fixtures.

## Phase 3: Enterprise Runtime

- Add Spring WebFlux operational APIs, metrics, tracing hooks, structured logging, and health checks.
- Add SQLite session metadata and benchmark history storage.
- Add Testcontainers-backed integration tests.
- Add JavaFX desktop app and Kotlin Android app.

## Phase 4: Advanced Optical Layer

- Evaluate multi-QR symbols, RGB multiplexing, fountain codes, RaptorQ, hybrid ARQ, and optical OFDM.
- Add AI-assisted decoder only if classical OpenCV and ZXing paths cannot meet measured targets.
