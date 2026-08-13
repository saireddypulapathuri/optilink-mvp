# Docker And Free Deployment Guide

This guide teaches how to run OptiLink as a Dockerized web app and deploy it to a free hosted web service.

## What You Are Building

OptiLink has two browser workflows:

- Sender: choose text, image, video, or file; encrypt it; split it into QR frames; display the animated QR beam.
- Receiver: open the same app on another device; start the camera; scan QR frames; reconstruct and decrypt the payload.

Camera scanning needs HTTPS on phones. `http://localhost` works for local desktop testing, but mobile browsers usually require HTTPS for camera access. Render gives deployed web services a public HTTPS URL.

## Files Added For Docker

- `Dockerfile`: builds the Java server jar in a Maven image, then runs it in a smaller Java runtime image.
- `.dockerignore`: keeps build output and unrelated modules out of the Docker build context.
- `docker-compose.yml`: local one-command Docker run with a named volume.
- `render.yaml`: optional Render blueprint for a free Docker web service.

## Local Docker Run

From the repository root:

```bash
cd /Users/psr/Documents/Opt
docker compose up --build
```

Open:

```text
http://localhost:8080
```

Stop:

```bash
docker compose down
```

Remove the local Docker transfer volume:

```bash
docker compose down -v
```

## Plain Docker Commands

Build:

```bash
docker build -t optilink:local .
```

Run:

```bash
docker run --rm -p 8080:8080 \
  -e PORT=8080 \
  -e OPTILINK_TRANSFER_ROOT=/data/optilink-product-transfers \
  -e OPTILINK_MAX_PAYLOAD_BYTES=131072 \
  optilink:local
```

## Free Render Deployment

Render supports Docker-based web services built from a repository Dockerfile. Render also supports free web services for hobby/testing use and provides managed TLS certificates for public URLs.

Official docs:

- [Docker on Render](https://render.com/docs/docker)
- [Deploy for Free](https://render.com/docs/free)
- [Your First Render Deploy](https://render.com/docs/your-first-deploy)

### Step 1: Push The Project To GitHub

Create a GitHub repository and push this project.

```bash
cd /Users/psr/Documents/Opt
git init
git add .
git commit -m "Build OptiLink QR scan MVP"
git branch -M main
git remote add origin https://github.com/<your-user>/<your-repo>.git
git push -u origin main
```

Do not run the commit commands if this folder is already part of your own repository history; adapt them to your current branch.

### Step 2: Create The Render Web Service

1. Sign in to Render.
2. Click `New`.
3. Choose `Web Service`.
4. Connect your GitHub repository.
5. Set runtime/language to `Docker`.
6. Keep Dockerfile path as `./Dockerfile`.
7. Choose the free instance type.
8. Add environment variables:

```text
OPTILINK_TRANSFER_ROOT=/data/optilink-product-transfers
OPTILINK_MAX_PAYLOAD_BYTES=131072
```

Render sets `PORT` automatically for web services. The Docker entrypoint uses it.

### Step 3: Deploy

Click `Create Web Service` or `Deploy`.

When the build finishes, Render gives a URL like:

```text
https://optilink.onrender.com
```

Open that URL on two devices:

1. On device A, open `/send`, create a transfer, and click `Open Display`.
2. On device B, open `/receive`, enter the same passphrase under `Receive`, and click `Start Camera`.
3. Point device B camera at device A QR beam.
4. Wait until the receiver says `Complete`.

Example:

```text
https://your-render-app.onrender.com/send
https://your-render-app.onrender.com/receive
```

## Important Free-Tier Limits

Render free web services are good for learning and demos, not production. Important practical effects:

- The filesystem is ephemeral on free web services, so generated transfers can disappear after restart/redeploy/spin-down.
- Free services can spin down and cold-start.
- Free services are single-instance and not designed for high throughput.
- Large files produce many QR frames. Keep MVP demos small: text, small images, and short preview clips.

## How The Docker Image Works

The Dockerfile has two stages:

1. Build stage: Maven compiles only the modules needed by `optilink-server`.
2. Runtime stage: a Java 21 JRE runs the packaged Spring Boot jar.

The app is configured by environment variables:

| Variable | Purpose | Default |
| --- | --- | --- |
| `PORT` | HTTP port used by the server | `8080` |
| `OPTILINK_TRANSFER_ROOT` | Where generated QR frames are stored | `/data/optilink-product-transfers` |
| `OPTILINK_MAX_PAYLOAD_BYTES` | Maximum source payload size | `131072` |

## Learning Checklist

Use this sequence to build skill:

1. Run `mvn test` and read the module list.
2. Run the jar locally and create a text transfer.
3. Open the generated display page and observe frame count.
4. Use the receiver panel with a camera-capable browser.
5. Build with `docker build`.
6. Run with `docker run`.
7. Deploy on Render.
8. Test phone-to-laptop and laptop-to-phone scan flows.

## Troubleshooting

- Camera button fails on mobile: use the HTTPS Render URL, not `http://<laptop-ip>:8080`.
- Camera permission denied: reset the browser site permission and try again.
- Receiver stays at `0 / 0`: move the camera closer and ensure the QR fills most of the preview.
- Invalid frames increase: slow the sender beam using the `Slow` speed button.
- Deploy builds slowly: the Docker build compiles Java inside the cloud builder; first build is usually the slowest.
