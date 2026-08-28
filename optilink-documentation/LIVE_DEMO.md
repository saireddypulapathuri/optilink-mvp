# Live Demo Runbook

Use this guide to demo OptiLink on two real devices.

## Active Demo URLs

When the local server and public tunnel are running, use these URLs:

```text
Console:     https://many-singers-refuse.loca.lt/
Transmitter: https://many-singers-refuse.loca.lt/send
Receiver:    https://many-singers-refuse.loca.lt/receive
```

If LocalTunnel shows a safety page, enter the tunnel password printed from:

```bash
curl -s https://loca.lt/mytunnelpassword
```

For the current demo session, that value was:

```text
49.43.241.117
```

Local equivalents:

```text
Console:     http://localhost:8080/
Transmitter: http://localhost:8080/send
Receiver:    http://localhost:8080/receive
```

## Start The Demo

From the repository root:

```bash
mvn -pl optilink-server -am -DskipTests package
java -jar optilink-server/target/optilink-server-0.1.0-SNAPSHOT.jar \
  --server.port=8080 \
  --optilink.transfer.root=target/optilink-product-transfers \
  --optilink.transfer.max-payload-bytes=131072
```

In a second terminal:

```bash
npx --yes localtunnel --port 8080
```

Use the URL printed by LocalTunnel. Replace the sample URLs above if a new tunnel URL is assigned.

## Laptop To Phone Demo

1. Open the transmitter URL on the laptop.
2. Choose `Text`, `Image`, `Video`, or `File`.
3. Enter a passphrase with at least 8 characters.
4. Click `Create Transfer`.
5. Click `Open Display`.
6. Copy the receiver link or enter the eight-character room code on the phone.
7. Enter the same passphrase.
8. Tap `Start Camera` and allow camera access.
9. Keep the moving QR inside the green corners until progress reaches 100%.
10. Preview or download the automatically recovered payload.

## Phone To Laptop Demo

1. Open the transmitter URL on the phone.
2. Create a small transfer.
3. Tap `Open Display`.
4. Open the receiver URL on the laptop.
5. Enter the same passphrase.
6. Click `Start Camera`.
7. Point the laptop camera at the phone QR beam.
8. Preview or download the recovered payload.

## Recommended Payloads

For the practical MVP, use:

- Text messages.
- Small screenshots or images under 128 KiB.
- Very short preview video clips under 128 KiB.
- Small binary files.

QR optical transfer is intentionally bandwidth-limited. Larger videos need the future high-throughput optical profile from the roadmap.

## Demo Troubleshooting

- Camera does not start: use the HTTPS tunnel or Render URL, not plain HTTP on a phone.
- Safety page appears: enter the LocalTunnel password.
- Receiver shows invalid frames: click `Slow` on the QR beam page, raise screen brightness, and move closer.
- Receiver does not progress: keep the QR inside the green corners, avoid glare, and use current Safari or Chrome.
- Camera is blocked on iPhone: allow camera access for the Render site in Safari settings, then reload `/receive`.
- Room expired: create a new sender room and open its new receiver link.
- Payload does not decrypt: check that both devices used the exact same passphrase.
