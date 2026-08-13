# Security Model

## Requirements

OptiLink treats every optical peer as untrusted until authenticated. It must not accept plaintext application payloads, hardcoded secrets, insecure algorithms, replayed packets, reused nonces, or unauthenticated session state.

## Implemented Phase 1 Controls

- Ed25519 identity key generation, signing, and verification.
- X25519 ephemeral key agreement for forward secrecy.
- HKDF-SHA-256 extract and expand for directional traffic keys.
- AES-256-GCM for authenticated encryption.
- 96-bit GCM nonces with a 32-bit session prefix and 64-bit monotonic counter.
- Replay window with duplicate and expired sequence rejection.
- Transcript hashing for session-key context binding.
- Key rotation policy model.
- MVP passphrase sealed payloads using PBKDF2-HMAC-SHA256 and AES-256-GCM.

## Threat Model

| Threat | Mitigation |
| --- | --- |
| Passive camera observer records optical frames | Payloads are encrypted with per-session AEAD keys. |
| Malicious device displays forged packets | AEAD authentication and Ed25519-authenticated handshakes reject forged data. |
| Replay of previous valid frames | Sequence validation and replay windows reject duplicates and old packets. |
| Optical corruption from blur or perspective distortion | CRC32C rejects corrupted protocol frames before decrypt; ZXing QR rendering applies QR Reed-Solomon at the symbol layer. |
| Key compromise after capture | X25519 ephemeral agreement gives forward secrecy when ephemeral private keys are destroyed. |
| Nonce reuse | `NonceSequence` creates monotonic 96-bit nonces per traffic key direction. |
| MVP passphrase disclosure | The passphrase is never stored in source, but users must choose and protect a strong offline passphrase. |

## Rejected Alternatives

- RSA key exchange was rejected because it does not provide forward secrecy.
- Static shared secrets were rejected because they create operational key-distribution and compromise risks.
- Plain CRC-only packet protection was rejected because CRC is not cryptographic authentication.
- Custom block ciphers or signature schemes were rejected because production security should use reviewed primitives and platform providers.

## Remaining Security Work

- Persist long-lived identity keys in platform secure storage.
- Bind identities to an operator-approved trust-on-first-use or enterprise certificate workflow.
- Add formal session transcript serialization for interoperable clients.
- Add negative tests for malformed key updates and replay boundary conditions.
- Add security fuzzing and mutation testing in CI.
- Replace MVP passphrase exchange with full mutual-auth handshake for enterprise deployment.
