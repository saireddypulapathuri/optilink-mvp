# CI/CD Pipeline

OptiLink uses GitHub Actions for continuous integration and Render for free hosted deployment.

## Pipeline Overview

```text
git push / pull request
        |
        v
GitHub Actions CI
        |
        +--> mvn test
        +--> package optilink-server jar
        +--> docker build validation
        |
        v
Render auto-deploy or Render deploy hook
        |
        v
public HTTPS app URL
```

## Workflow Files

- `.github/workflows/ci.yml`: runs tests, packages the Spring Boot server jar, and validates the Docker image build.
- `.github/workflows/deploy-render.yml`: triggers a Render deploy hook after a successful `main` CI run or by manual dispatch.
- `.github/dependabot.yml`: opens weekly update PRs for Maven dependencies and GitHub Actions.
- `render.yaml`: Render blueprint for a free Docker web service.
- `Dockerfile`: multi-stage Docker build used by Render and CI.

## GitHub Secrets

Add this repository secret after the Render service exists:

```text
RENDER_DEPLOY_HOOK_URL=<Render deploy hook URL>
```

Render can also auto-deploy on every push to `main`. The deploy hook workflow is useful when you want GitHub Actions to be the explicit release gate.

## Render Service Settings

Use these values for the Render web service:

```text
Runtime: Docker
Dockerfile path: ./Dockerfile
Instance type: Free
Branch: main
Health check path: /health
```

Environment variables:

```text
OPTILINK_TRANSFER_ROOT=/data/optilink-product-transfers
OPTILINK_MAX_PAYLOAD_BYTES=131072
```

Render provides `PORT` automatically. The Docker entrypoint reads it.

## Release Flow

1. Commit changes locally.
2. Push to `main`.
3. GitHub Actions runs tests and Docker build validation.
4. Render deploys the Docker app.
5. Test the public URLs:

```text
https://<render-service>.onrender.com/
https://<render-service>.onrender.com/send
https://<render-service>.onrender.com/receive
https://<render-service>.onrender.com/health
```

## Live Test Checklist

1. Open `/send` on device A.
2. Create a small text, image, video, or file transfer.
3. Open the generated QR beam.
4. Open `/receive` on device B.
5. Enter the same passphrase.
6. Start the camera and scan the beam.
7. Verify recovered preview or download.
