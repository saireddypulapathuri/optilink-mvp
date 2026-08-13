# Testing Strategy

## Implemented Tests

`mvn test` currently runs 29 tests across module and integration suites:

- Defensive immutability in core payloads.
- Protocol packet encode/decode and CRC32C corruption rejection.
- Selective ACK encode/decode.
- MVP transfer chunk encode/decode and reassembly.
- HKDF RFC 5869 vector validation.
- AES-GCM encryption, decryption, and AAD tamper rejection.
- Ed25519 signing/verification and X25519 agreement.
- Passphrase sealed-payload round trip and wrong-passphrase rejection.
- Replay-window duplicate and expiry rejection.
- Directional session key derivation.
- Secure session key rotation deadline detection.
- Congestion control growth and reduction.
- Reliable sender chunking, windowing, and retransmission.
- Reliable receiver out-of-order buffering and SACK state.
- Optical profile selection.
- QR payload envelope validation.
- Encoder/decoder packet-symbol round trip.
- QR PNG rendering and image decoding.
- Luminance frame quality analysis.
- Compression round trip.
- Cross-module protocol flow through QR PNG render/decode.
- Practical MVP encrypted QR transfer write/read flow.

## Required Future Suites

- Property tests for packet parser invariants and ACK range normalization.
- Fuzz tests for malformed frames, malformed ACKs, invalid nonce inputs, and decoder payloads.
- Concurrency tests for sender, receiver, nonce sequence, and replay window under virtual threads.
- OpenCV-based camera integration tests with generated frame fixtures.
- Testcontainers scenarios for server health and operational APIs.
- Mutation testing for protocol, security, and transport critical paths.
- Hardware-in-loop optical tests across brightness, distance, angle, FPS, and motion.

## Quality Gate

No phase is complete unless the reactor compiles, tests pass, documentation is updated, benchmark history is recorded, and security/architecture risks are explicitly listed.
