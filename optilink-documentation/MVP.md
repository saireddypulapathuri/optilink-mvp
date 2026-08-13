# Practical MVP

## Scope

The practical MVP supports one-way offline optical transfer using generated QR PNG frames. A sender encrypts text, image, video, or file bytes with a user-provided passphrase, splits the sealed payload into protocol chunks, renders QR frames, and writes a local `index.html` page that beams those frames for display. A receiver can scan the beam from a browser camera or decode a directory of captured/generated QR PNG frames, reassemble the transfer, authenticate AES-GCM, and recover plaintext.

This MVP is useful for validating the optical protocol pipeline on commodity devices before the full mutual-auth camera application exists.

## Web Product

Run the local product console:

```bash
mvn -f optilink-server/pom.xml spring-boot:run -Dspring-boot.run.arguments="--server.port=8080 --optilink.transfer.root=target/optilink-product-transfers"
```

Open `http://localhost:8080` and use the Create, Receive, Transfers, and Current Transfer panels to generate, display, scan, inspect, and receive encrypted QR transfers. The web product has dedicated `Text`, `Image`, `Video`, and `File` modes; recovered images and videos preview in the browser with a download link.

## Send

```bash
export OPTILINK_PASSPHRASE='choose a strong shared passphrase'
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args='send --out /tmp/optilink-transfer --text hello'
open /tmp/optilink-transfer/index.html
```

For a file:

```bash
export OPTILINK_PASSPHRASE='choose a strong shared passphrase'
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args='send --out /tmp/optilink-transfer --file /path/to/message.txt'
```

## Receive

```bash
export OPTILINK_PASSPHRASE='choose a strong shared passphrase'
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args='receive --in /tmp/optilink-transfer'
```

To write recovered bytes:

```bash
export OPTILINK_PASSPHRASE='choose a strong shared passphrase'
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args='receive --in /tmp/optilink-transfer --out /tmp/recovered.bin'
```

## Inspect

Inspection decodes the QR frames and reports completeness without requiring the passphrase:

```bash
mvn -q -f optilink-cli/pom.xml exec:java -Dexec.mainClass=com.optilink.cli.OptiLinkCli -Dexec.args='inspect --in /tmp/optilink-transfer'
```

## Transfer Format

- Plaintext is sealed with PBKDF2-HMAC-SHA256 and AES-256-GCM.
- The sealed payload stores PBKDF2 iterations, salt, nonce, and ciphertext.
- Sealed bytes are split into `TransferChunk` records.
- Each chunk is wrapped in an OptiLink `DATA` packet.
- Each packet is encoded as an `OpticalSymbol`.
- Each symbol is rendered as a QR PNG frame.
- `optilink-transfer.properties` records the transfer id, frame count, QR profile, and display cadence for operator diagnostics.

## Security Notes

Passphrase MVP mode is not the final enterprise authentication model. It does not provide mutual device authentication, device identity enrollment, or X25519 forward secrecy. It does provide confidentiality and tamper detection for the sealed payload when the passphrase is strong and kept offline.

The production session model remains Ed25519 identity plus X25519 ephemeral agreement, HKDF, AES-GCM, nonce discipline, and replay protection.

## Practical Capture Workflow

For a real device-to-device trial today, open the web product on both devices. Create a transfer on the sender, open the generated `OptiLink Beam`, then start the camera receiver on the other device and point it at the beam. For CLI trials, the receiver can still read captured or generated QR PNG files from a directory.

## Known Limitations

- No OpenCV perspective correction yet.
- Missing frames are reported by inspection, but there is no automatic re-request path yet.
- No mutual-auth handshake in MVP passphrase mode.
- QR frame count grows with payload size; this is suitable for short messages, small images, short preview clips, and small files.
