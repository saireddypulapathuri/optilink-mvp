# OptiLink Server

## Architecture Overview

`optilink-server` is a Spring WebFlux product adapter. It serves the local OptiLink web console, REST transfer APIs for text, image, video, and file payloads, browser camera scan APIs, generated QR display pages, generated QR PNG frames, transfer manifests, and health checks.

## Design Goals

- Keep operational endpoints separate from domain modules.
- Provide a usable local product surface for encrypted QR transfer workflows.
- Serve generated transfer assets from a constrained storage root.
- Provide health checks for deployments.

## Diagrams

Server is an adapter in `../optilink-documentation/ARCHITECTURE.md`.

## Protocol Specification and Packet Format

Server delegates optical packet formats and QR transfer logic to `optilink-transfer`.

## State Machine

Server lifecycle is Spring-managed. Protocol session state remains in `optilink-session`.

## API Documentation

Public API: `OptiLinkServerApplication`, `HealthController`, `TransferController`, `ProductTransferService`, request/response records, and health/transfer views.

Primary routes:

- `GET /`
- `GET /health`
- `GET /api/transfers`
- `POST /api/transfers/text`
- `POST /api/transfers/image`
- `POST /api/transfers/video`
- `POST /api/transfers/file`
- `GET /api/transfers/{transferId}`
- `GET /api/transfers/{transferId}/inspect`
- `POST /api/transfers/{transferId}/receive`
- `POST /api/scans`
- `GET /api/scans/{sessionId}`
- `POST /api/scans/{sessionId}/frames`
- `POST /api/scans/{sessionId}/receive`
- `GET /transfers/{transferId}/{fileName}`

## Security Model and Threat Model

Operational APIs must not persist passphrases or expose sealed transfer internals. Plaintext is returned only after the caller supplies the passphrase for the selected local transfer.

## Performance Model and Benchmarks

Server endpoint benchmarks are future work.

## Deployment Guide

Run from the repository root:

```bash
mvn -f optilink-server/pom.xml spring-boot:run -Dspring-boot.run.arguments="--server.port=8080 --optilink.transfer.root=target/optilink-product-transfers"
```

Open `http://localhost:8080`.

Or package and run the executable jar:

```bash
mvn -pl optilink-server -am -DskipTests package
java -jar optilink-server/target/optilink-server-0.1.0-SNAPSHOT.jar --server.port=8080 --optilink.transfer.root=target/optilink-product-transfers
```

Set `--optilink.transfer.max-payload-bytes=<bytes>` to tune the local payload ceiling. Larger images and clips generate more QR frames.

Docker and free Render deployment are covered in `../optilink-documentation/DOCKER_DEPLOYMENT.md`.

## Developer Guide

Keep WebFlux annotations in this module only.

## Contribution Guide

Add tests for every endpoint.

## Extension Guide

Future endpoints can expose missing-frame recovery, metrics, benchmark history, durable scan sessions, and policy configuration.

## Tradeoffs, Lessons Learned, Known Limitations, and Future Roadmap

The server now exposes the local product MVP, including end-to-end image and video byte recovery through encrypted QR frames plus browser camera scan sessions. Perspective correction remains future work.
