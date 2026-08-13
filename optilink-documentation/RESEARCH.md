# Research Summary

This summary records Phase 1 research and production decisions. The implementation uses practical techniques that work on consumer displays and cameras today while keeping space for higher-throughput physical layers later.

## References

- IEEE 802.15.7-2018 defines short-range optical wireless communication PHY and MAC concepts: https://ieeexplore.ieee.org/document/8697198
- IEEE 802.15.7a-2024 amends 802.15.7 with higher-rate, longer-range optical camera communication: https://standards.ieee.org/ieee/802.15.7a/10367/
- RFC 9000 defines QUIC transport ideas such as connection IDs, streams, ACKs, and encrypted transport: https://datatracker.ietf.org/doc/rfc9000/
- RFC 9002 defines QUIC loss detection and congestion control: https://datatracker.ietf.org/doc/html/rfc9002
- RFC 8446 defines TLS 1.3 and its modern handshake/security model: https://datatracker.ietf.org/doc/html/rfc8446
- RFC 5869 defines HKDF: https://datatracker.ietf.org/doc/html/rfc5869
- RFC 7748 defines X25519 and related curves: https://datatracker.ietf.org/doc/html/rfc7748
- RFC 8032 defines EdDSA including Ed25519: https://datatracker.ietf.org/doc/html/rfc8032
- NIST SP 800-38D specifies AES-GCM and GMAC: https://csrc.nist.gov/pubs/sp/800/38/d/final
- ZXing documents QR error correction levels from ISO 18004: https://zxing.github.io/zxing/apidocs/com/google/zxing/qrcode/decoder/ErrorCorrectionLevel.html

## Topic Decisions

| Topic | Advantages | Disadvantages | Production applicability | Phase 1 decision | Rejected alternatives |
| --- | --- | --- | --- | --- | --- |
| Optical Camera Communication | Directional, offline, no radios required | Sensitive to blur, lighting, exposure, rolling shutter | Strong fit for secure nearby exchange | Use OCC as the system channel model | Wi-Fi/Bluetooth/NFC/USB |
| Optical Wireless Communication | Mature standards vocabulary and PHY/MAC research | Many techniques assume LED hardware, not consumer screens | Useful for terminology and future PHYs | Keep protocol independent from rendering | Custom hardware-only design |
| Dynamic QR | Works on commodity screens and cameras | Limited capacity and visible symbols | Best practical first optical carrier | Implement ZXing-backed PNG rendering and decoding behind adapter modules | From-scratch exotic modulation |
| Display to Camera Communication | Device-to-device with no pairing hardware | Human alignment and camera quality matter | Practical with adaptive profiles | Add channel metrics and profile selection | Fixed-rate one-size encoding |
| Adaptive QR Encoding | Trades FPS, QR version, payload, and ECC | Needs feedback loop and calibration | Essential for real devices | Implement selector with robust/balanced/throughput profiles and QR renderer hints | Static QR version |
| Forward Error Correction | Improves burst-loss tolerance | Reduces payload capacity and adds complexity | Use QR Reed-Solomon first | Keep Layer 1 QR ECC and roadmap fountain codes | Immediate RaptorQ dependency |
| Reliable Transport | Recovers loss over visual frames | Adds latency and state | Required for application messages | Sliding window, SACK, timeout, fast retransmit | Fire-and-forget frames |
| Congestion Control | Avoids overwhelming camera/decoder | Needs tuning on hardware | Required for adaptive optical links | AIMD with optical feedback | Fixed unlimited window |
| Rate Adaptation | Sustains throughput under variable lighting/motion | Can oscillate without damping | Required in mobile use | Select optical profiles from FPS, blur, decode ratio | Manual operator tuning only |
| Hybrid ARQ | Efficiently combines FEC and retransmit | More protocol state and soft-decision data | Future work after renderer/camera loop | Defer until measured loss patterns exist | Premature HARQ implementation |
| Visible Light Communication | Radio-free and intuitive proximity security | Ambient light and line-of-sight constraints | Applicable at application layer | Use visible display-camera path | Infrared-only hardware path |
| Camera Calibration | Improves perspective correction | Platform-specific data collection | Needed for robust decoding | Add calibration SPI and luminance quality analyzer, defer OpenCV implementation | No calibration model |
| Motion Blur Compensation | Raises decode success while moving | Computationally expensive on phones | Needed for later camera adapter | Estimate blur from luminance Laplacian variance and feed blur into profile selector | Heavy AI decoder in Phase 1 |
| Frame Synchronization | Needed to detect symbol boundaries | Hard across camera exposure rates | Required for real renderer | Model sequence numbers and profiles | Timing-only sync |
| Perspective Correction | Essential when cameras are angled | Requires image processing | Adapter responsibility | Keep camera module boundary | Embed OpenCV in protocol core |
| Zero Trust Architecture | Rejects implicit trust in nearby devices | Requires identity UX and key storage | Mandatory for enterprise use | Ed25519/X25519 session model | Anonymous plaintext exchange |
| Mutual Authentication | Prevents rogue visual peer | Needs identity enrollment | Mandatory before data | Implement identity signing primitives | One-way authentication |
| Forward Secrecy | Reduces recorded-traffic risk | Requires ephemeral key lifecycle | Mandatory | X25519 ephemeral agreement | Static RSA or PSK-only |
| Replay Protection | Blocks recorded frame reuse | Requires sequence windows | Mandatory | ReplayWindow plus transport sequence numbers | Timestamp-only replay defense |
| Packet Authentication | Detects active modification | AEAD nonce discipline required | Mandatory | AES-256-GCM with AAD hooks | CRC as authentication |
| Packet Scheduling | Keeps camera decode load stable | Needs feedback and queue policy | Required for reliability | Window-based scheduler | Display every frame as soon as possible |

## Practicality Notes

RGB multiplexing, optical OFDM, AI decoders, RaptorQ, and hybrid ARQ are promising but impractical as first implementation choices for consumer devices without measured hardware data. The stack deliberately chooses QR-compatible symbols, conservative payload sizing, and application-level reliability so the protocol can be validated before optimizing the physical layer.
