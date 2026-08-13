# Setup And Running Guide

## Requirements

- macOS, Linux, or Windows with a terminal.
- JDK 21 or newer. The project compiles with Java 21 release bytecode.
- Apache Maven 3.9 or newer.
- A modern browser for the web product.

Check your local tools:

```bash
java -version
mvn -version
```

## Build And Test

From the repository root:

```bash
cd /Users/psr/Documents/Opt
mvn test
```

Expected result:

```text
BUILD SUCCESS
```

## Run The Web Product

Start the local OptiLink web app:

```bash
cd /Users/psr/Documents/Opt
mvn -f optilink-server/pom.xml spring-boot:run -Dspring-boot.run.arguments="--server.port=8080 --optilink.transfer.root=target/optilink-product-transfers"
```

Open:

```text
http://localhost:8080
```

If port `8080` is busy, change the port:

```bash
mvn -f optilink-server/pom.xml spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --optilink.transfer.root=target/optilink-product-transfers"
```

You can also package and run the executable jar:

```bash
mvn -pl optilink-server -am -DskipTests package
java -jar optilink-server/target/optilink-server-0.1.0-SNAPSHOT.jar --server.port=8080 --optilink.transfer.root=target/optilink-product-transfers
```

The default web payload ceiling is `131072` bytes. For a demo with larger images or very short clips, add a higher value and expect a larger QR frame count:

```bash
java -jar optilink-server/target/optilink-server-0.1.0-SNAPSHOT.jar --server.port=8080 --optilink.transfer.root=target/optilink-product-transfers --optilink.transfer.max-payload-bytes=262144
```

## Web Product Workflow

1. Open `http://localhost:8080`.
2. Choose `Text`, `Image`, `Video`, or `File`.
3. Enter a passphrase with at least 8 characters.
4. Click `Create Transfer`.
5. Use `Open Display` to show the generated QR frame loop.
6. Use `Inspect` to verify that all generated frames are readable.
7. Enter the same passphrase under `Current Transfer`.
8. Click `Receive` to recover the payload. Images and videos preview in the browser and can be downloaded.

## Two-Device Camera Workflow

Use this for the Decimen-style mobile scan flow:

1. Open the app on the sender device.
2. Create a `Text`, `Image`, `Video`, or `File` transfer.
3. Click `Open Display`; the sender shows an animated `OptiLink Beam`.
4. Open the app on the receiver device.
5. Enter the same passphrase in the `Receive` panel.
6. Click `Start Camera`.
7. Point the receiver camera at the sender QR beam.
8. Wait for `Complete`, then preview or download the recovered payload.

For phone browsers, use an HTTPS deployment URL. Local `http://localhost:8080` is fine for desktop testing, but mobile camera access usually requires HTTPS. See [Docker And Free Deployment Guide](DOCKER_DEPLOYMENT.md).

Generated transfer data is stored under:

```text
/Users/psr/Documents/Opt/target/optilink-product-transfers
```

Each transfer directory contains:

- `index.html`: local display page that loops the QR frames.
- `frame-0001-of-000N.png`: QR frame images.
- `optilink-transfer.properties`: transfer id, frame count, QR profile, and frame cadence.
- `optilink-product.properties`: product metadata used by the web UI, including payload kind and content type.

## Run The CLI MVP

Set a passphrase:

```bash
export OPTILINK_PASSPHRASE="choose a strong shared passphrase"
```

Create an encrypted text transfer:

```bash
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args="send --out target/cli-transfer --text hello"
```

Inspect completeness:

```bash
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args="inspect --in target/cli-transfer"
```

Recover plaintext:

```bash
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args="receive --in target/cli-transfer"
```

Create an encrypted file transfer:

```bash
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args="send --out target/file-transfer --file /path/to/file.bin"
```

Recover a file:

```bash
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args="receive --in target/file-transfer --out target/recovered.bin"
```

## REST API

Create a text transfer:

```bash
curl -s -X POST http://localhost:8080/api/transfers/text \
  -H "Content-Type: application/json" \
  -d '{"text":"hello","passphrase":"correct horse battery staple"}'
```

Create an image transfer:

```bash
curl -s -X POST http://localhost:8080/api/transfers/image \
  -H "Content-Type: application/json" \
  -d '{"fileName":"pixel.png","contentType":"image/png","contentBase64":"<base64-image-bytes>","passphrase":"correct horse battery staple"}'
```

Create a video transfer:

```bash
curl -s -X POST http://localhost:8080/api/transfers/video \
  -H "Content-Type: application/json" \
  -d '{"fileName":"clip.mp4","contentType":"video/mp4","contentBase64":"<base64-video-bytes>","passphrase":"correct horse battery staple"}'
```

Create a generic file transfer:

```bash
curl -s -X POST http://localhost:8080/api/transfers/file \
  -H "Content-Type: application/json" \
  -d '{"fileName":"archive.bin","contentType":"application/octet-stream","contentBase64":"<base64-file-bytes>","passphrase":"correct horse battery staple"}'
```

List transfers:

```bash
curl -s http://localhost:8080/api/transfers
```

Inspect a transfer:

```bash
curl -s http://localhost:8080/api/transfers/<transfer-id>/inspect
```

Receive a transfer:

```bash
curl -s -X POST http://localhost:8080/api/transfers/<transfer-id>/receive \
  -H "Content-Type: application/json" \
  -d '{"passphrase":"correct horse battery staple"}'
```

The receive response includes `kind`, `name`, `contentType`, `base64`, and `dataUrl` so clients can preview or download recovered media.

Create a live scan session:

```bash
curl -s -X POST http://localhost:8080/api/scans \
  -H "Content-Type: application/json" \
  -d '{"passphrase":"correct horse battery staple"}'
```

Submit a camera frame:

```bash
curl -s -X POST http://localhost:8080/api/scans/<session-id>/frames \
  -H "Content-Type: application/json" \
  -d '{"imageBase64":"<png-or-jpeg-frame-base64>"}'
```

Receive a completed scan:

```bash
curl -s -X POST http://localhost:8080/api/scans/<session-id>/receive
```

## Benchmarks

```bash
mvn -q -f optilink-benchmark/pom.xml exec:java -Dexec.mainClass=com.optilink.benchmark.ProtocolBenchmark
mvn -q -f optilink-benchmark/pom.xml exec:java -Dexec.mainClass=com.optilink.benchmark.QrRenderDecodeBenchmark
mvn -q -f optilink-benchmark/pom.xml exec:java -Dexec.mainClass=com.optilink.benchmark.MvpTransferBenchmark
```

## Troubleshooting

- `passphrase must be at least 8 characters`: use a longer passphrase.
- `failed to decode QR optical frame`: regenerate the transfer, slow the beam, move closer, or reduce blur/glare.
- Camera does not start on mobile: use an HTTPS URL and allow camera permission.
- `payload exceeds ... bytes`: choose a smaller file or raise `--optilink.transfer.max-payload-bytes`, knowing this increases QR frames.
- `port 8080 already in use`: run with `--server.port=8081`.
- Maven `Unsafe` warnings on newer JDKs are dependency warnings; the verified build still passes.
- Browser camera capture is available in the web receiver. OpenCV perspective correction remains a future hardening stage.
