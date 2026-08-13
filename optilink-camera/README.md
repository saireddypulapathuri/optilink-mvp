# OptiLink Camera

## Architecture Overview

`optilink-camera` defines the platform camera adapter SPI, calibration values, and luminance frame-quality analysis.

## Design Goals

- Keep camera capture replaceable across desktop and Android.
- Model calibration without locking into OpenCV classes.
- Preserve timestamped frame data for synchronization.
- Produce brightness, contrast, and blur feedback for optical adaptation.

## Diagrams

Camera adapter flow is shown in `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

Camera frames are not protocol packets. Decoder modules extract symbols from frames.

## State Machine

Adapters may maintain camera lifecycle state outside this SPI.

## API Documentation

Public API: `CameraAdapter`, `CameraFrame`, `CameraCalibration`, `CameraFrameQuality`, `LuminanceFrameAnalyzer`.

## Security Model and Threat Model

Camera input is attacker-controlled until decoded, authenticated, and replay-checked.

## Performance Model and Benchmarks

Current quality analysis is unit-tested. Future benchmarks will measure capture FPS, preprocessing cost, blur, and decode success.

## Deployment Guide

Platform modules supply concrete camera adapters.

## Developer Guide

Keep frame format explicit in adapter documentation.

## Contribution Guide

Validate dimensions and calibration parameters.

## Extension Guide

Add exposure, rolling-shutter, and focus metadata as measured needs appear.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The module now includes SPI plus grayscale quality analysis. OpenCV implementation is still pending.
