# OptiLink Optical

## Architecture Overview

`optilink-optical` models channel metrics, symbol profiles, and encoded optical symbols.

## Design Goals

- Adapt FPS, QR version, ECC level, brightness, and payload capacity from feedback.
- Keep visual rendering independent from protocol framing.
- Preserve future support for multi-QR and non-QR carriers.

## Diagrams

Optical adaptation appears in `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

Optical symbols carry already-framed protocol bytes. They do not define protocol headers.

## State Machine

The profile selector is stateless and deterministic.

## API Documentation

Public API: `OpticalChannelMetrics`, `OpticalProfile`, `OpticalSymbol`, `RenderedOpticalFrame`, `QrPayloadEnvelope`, `ErrorCorrectionLevel`, `AdaptiveOpticalProfileSelector`.

## Security Model and Threat Model

Optical profile changes never relax packet authentication. Attackers may influence channel metrics, so transport must bound adaptation.

## Performance Model and Benchmarks

Optical profile capacity and FPS determine theoretical throughput. QR PNG render/decode benchmarks are recorded in `../optilink-documentation/BENCHMARKS.md`.

## Deployment Guide

Start with robust profiles for uncalibrated devices.

## Developer Guide

Do not embed ZXing or OpenCV directly in this module; keep those as adapters.

## Contribution Guide

Add metrics with validation and tests.

## Extension Guide

Future profiles can describe RGB multiplexing, multi-symbol grids, or OFDM parameters.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The current stack models adaptation and QR PNG frames. OpenCV capture and perspective correction remain future work.
