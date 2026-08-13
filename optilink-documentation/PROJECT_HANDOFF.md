# OptiLink Project Handoff

## Project Summary

OptiLink is a browser-based offline file transfer app. A transmitter encrypts text, image, video, or file bytes, streams them as animated QR frames, and a receiver scans those frames with a mobile or desktop camera to reconstruct the payload.

## Tech Stack

- Java 21
- Spring Boot WebFlux
- Plain HTML, CSS, and browser JavaScript
- ZXing QR encode/decode
- AES-256-GCM passphrase sealing
- Maven multi-module build
- Docker multi-stage image
- Render free web service deployment

## Primary URLs

- `/`: full console with transmitter, receiver, transfer list, and transfer detail panes.
- `/send`: transmitter-focused UI.
- `/receive`: receiver-focused UI.
- `/transfers/{transferId}/index.html`: generated QR beam page.
- `/health`: deployment health check.

## Folder Structure

```text
.
├── .dockerignore
├── .env.example
├── .gitignore
├── Dockerfile
├── README.md
├── docker-compose.yml
├── render.yaml
├── pom.xml
├── optilink-core
├── optilink-protocol
├── optilink-security
├── optilink-session
├── optilink-congestion
├── optilink-transport
├── optilink-optical
├── optilink-encoder
├── optilink-decoder
├── optilink-camera
├── optilink-compression
├── optilink-transfer
├── optilink-api
├── optilink-desktop
├── optilink-android
├── optilink-server
├── optilink-cli
├── optilink-benchmark
├── optilink-tests
└── optilink-documentation
```

## File Dictionary

- `.dockerignore`: excludes build outputs and unrelated modules from Docker context.
- `.env.example`: documents Docker/runtime environment variables.
- `.gitignore`: ignores IDE, target, and local metadata files.
- `Dockerfile`: builds the Spring Boot server jar and runs it in a Java 21 runtime container.
- `docker-compose.yml`: local Docker Compose run configuration.
- `render.yaml`: Render blueprint for a free Docker web service.
- `pom.xml`: parent Maven build and module registry.
- `README.md`: top-level product, build, run, and documentation index.

### Core Modules

- `optilink-core`: shared immutable values and base exceptions.
- `optilink-protocol`: packet format, CRC integrity, chunking, and reassembly.
- `optilink-security`: AES-GCM, passphrase sealing, identity, key derivation, and replay protection primitives.
- `optilink-session`: production-shaped secure session construction.
- `optilink-congestion`: adaptive window control.
- `optilink-transport`: reliable sender and receiver primitives.
- `optilink-optical`: optical profiles and symbols.
- `optilink-encoder`: packet-to-QR rendering.
- `optilink-decoder`: QR image decoding back into protocol packets.
- `optilink-camera`: camera quality analysis boundaries.
- `optilink-compression`: deflate compression utilities.

### Product Modules

- `optilink-transfer`: product-level QR transfer service used by CLI and server.
- `optilink-server`: Spring Boot web product, REST API, static UI, and generated transfer serving.
- `optilink-cli`: command-line send, inspect, and receive workflow.
- `optilink-benchmark`: benchmark harnesses.
- `optilink-tests`: cross-module integration tests.
- `optilink-documentation`: architecture, setup, deployment, MVP, and handoff docs.

## Important Source Files

- `optilink-server/src/main/java/com/optilink/server/TransferController.java`: REST routes and `/send` plus `/receive` app-shell routes.
- `optilink-server/src/main/java/com/optilink/server/ProductTransferService.java`: create/list/inspect/receive generated transfers.
- `optilink-server/src/main/java/com/optilink/server/ScanSessionService.java`: in-memory browser camera scan sessions.
- `optilink-server/src/main/resources/static/index.html`: app shell for console, transmitter, and receiver.
- `optilink-server/src/main/resources/static/app.js`: UI state, file conversion, REST calls, camera capture loop, receive rendering.
- `optilink-server/src/main/resources/static/app.css`: responsive product layout and visual design.
- `optilink-transfer/src/main/java/com/optilink/transfer/QrTransferMvpService.java`: encrypted QR frame write/read/decode orchestration.
- `optilink-transfer/src/main/java/com/optilink/transfer/TransferDisplayPageWriter.java`: generated QR beam page.
- `optilink-protocol/src/main/java/com/optilink/protocol/TransferChunker.java`: splits sealed payloads.
- `optilink-protocol/src/main/java/com/optilink/protocol/TransferReassembler.java`: accepts chunks and reports missing frames.
- `optilink-security/src/main/java/com/optilink/security/PassphraseSealedBox.java`: passphrase-based encryption envelope.
- `optilink-server/src/test/java/com/optilink/server/TransferControllerTest.java`: web product regression tests.

## Data Flow

1. Sender chooses payload and passphrase in `app.js`.
2. Browser sends JSON to `TransferController`.
3. `ProductTransferService` validates payload metadata and calls `QrTransferMvpService`.
4. `QrTransferMvpService` encrypts with `PassphraseSealedBox`, chunks through protocol classes, and renders QR PNG frames.
5. Server stores generated frame files and metadata under `OPTILINK_TRANSFER_ROOT`.
6. Sender opens `/transfers/{transferId}/index.html`, generated by `TransferDisplayPageWriter`.
7. Receiver opens `/receive`, starts camera, and `app.js` uploads canvas snapshots to `/api/scans/{sessionId}/frames`.
8. `ScanSessionService` decodes each frame, accepts unique chunks, and reports progress.
9. When complete, `/api/scans/{sessionId}/receive` decrypts and returns text/base64/data URL.
10. Browser renders recovered text, media preview, or download link.

## Refactoring Map

- Change UI copy/layout: edit `index.html` and `app.css`.
- Change transmitter interactions: edit `app.js`, especially `createText`, `createBinary`, `setMode`, and route mode handling.
- Change receiver/camera behavior: edit `app.js`, especially `startScan`, `captureScanFrame`, `renderScan`, and `receiveScan`.
- Change REST contracts: edit `TransferController` and request/response records in `optilink-server/src/main/java/com/optilink/server`.
- Change transfer limits/content-type rules: edit `ProductTransferService`.
- Change QR frame size/speed/profile: edit `QrTransferMvpService` and `TransferDisplayPageWriter`.
- Change encryption or passphrase policy: edit `PassphraseSealedBox` and callers that validate passphrase length.
- Add durable storage/database: replace file metadata reads/writes in `ProductTransferService` and in-memory map in `ScanSessionService`.
- Add missing-frame resend/fountain codes: change `optilink-protocol` chunking/reassembly and `optilink-transfer` frame writer/reader.
- Add production auth/users: add a security module in `optilink-server`, then bind user ownership into transfer metadata.

## Verification Commands

```bash
mvn test
mvn -pl optilink-server -am -DskipTests package
java -jar optilink-server/target/optilink-server-0.1.0-SNAPSHOT.jar --server.port=8080 --optilink.transfer.root=target/optilink-product-transfers
```
