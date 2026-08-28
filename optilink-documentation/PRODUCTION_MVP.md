# Production MVP Architecture

OptiLink is a multi-session QR optical transfer MVP. It supports concurrent sender and receiver pairs on one service instance, with encrypted payloads moving through animated QR frames rather than through a network relay.

## Request Flow

```text
sender browser
  -> creates an expiring room
  -> encrypts text, image, video, or file data
  -> displays animated QR frames

receiver browser
  -> joins the room for isolated discovery
  -> decodes QR content on-device
  -> submits verified OptiLink envelopes to its scan session
  -> reassembles and decrypts after all unique frames arrive
  -> previews or downloads the original media type
```

## Multi-User Model

- Rooms use eight-character codes and expire after two hours.
- Every camera receiver has an independent UUID scan session that expires after 20 minutes.
- Transfer lists are scoped by the `X-OptiLink-Room` header.
- The room code coordinates devices. It is not the encryption key; payload protection uses the passphrase.
- Active rooms and scans are bounded to protect a small free instance from unbounded memory growth.

## Mobile Scanner

- Native `BarcodeDetector` is used when available.
- Bundled jsQR provides the Safari and iOS fallback without a runtime CDN dependency.
- The browser crops and downsizes the camera center before decoding.
- Decoded QR text is submitted instead of full camera images whenever possible.
- A compressed camera-frame fallback remains for older clients.
- The AR-style overlay supplies framing corners, an animated scan line, progress, missing-frame diagnostics, and automatic recovery.
- The encrypted optical payload includes kind, file name, and content type so scanned images and videos retain preview and download behavior.

## Production Controls

- Maximum plaintext transfer: 128 KiB by default.
- Maximum HTTP in-memory body: 3 MiB.
- Maximum encoded camera fallback: 2.5 million characters.
- Room TTL: two hours.
- Scan TTL: 20 minutes.
- Browser headers: CSP, no-sniff, no-referrer, camera-only permissions policy, and API no-store caching.
- Graceful shutdown: 20-second drain window.
- Render health check: `/health`.

## Scaling Boundary

The Render free deployment is a single process with ephemeral disk. Rooms and scans are in memory, so horizontal replicas require Redis or another shared low-latency store. Generated transfer artifacts require object storage before using multiple instances.

The scale-out phase should add Redis-backed room and scan state, S3-compatible frame storage, signed room capabilities, rate limiting, exported metrics, traces, and a higher-throughput optical profile for payloads above 128 KiB.

This is an honest production MVP boundary: safe concurrent use on one instance, with distributed state and durable storage clearly separated into the next phase.
