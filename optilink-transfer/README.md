# OptiLink Transfer

## Architecture Overview

`optilink-transfer` owns application-level encrypted transfer workflows that can be reused by CLI, server, desktop, or mobile adapters.

## Design Goals

- Keep product workflows out of transport and presentation adapters.
- Provide one tested path for QR frame generation, inspection, reassembly, and decryption.
- Preserve security boundaries: passphrases are inputs, not persisted secrets.

## API Documentation

Public API: `QrTransferMvpService`, `MvpTransferSummary`, and `MvpTransferInspection`.

## Security Model and Threat Model

The practical MVP uses passphrase-sealed AES-GCM payloads for offline one-way transfer. Production mutual authentication remains in the session/security modules.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The current transfer service generates and reads QR PNG directories. Live camera capture and automatic re-request are next-stage product work.
