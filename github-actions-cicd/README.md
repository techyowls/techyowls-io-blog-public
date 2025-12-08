# GitHub Actions CI/CD for Java/Spring Boot

Code samples for: [GitHub Actions CI/CD: Complete Pipeline from Push to Production](https://techyowls.io/blog/github-actions-cicd-java-spring-boot)

## Pipeline Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                           CI Pipeline (on push/PR)                    │
├──────────────────────────────────────────────────────────────────────┤
│  ┌─────────┐    ┌──────────┐    ┌───────────┐    ┌────────────────┐ │
│  │  Lint   │ -> │  Build   │ -> │   Test    │ -> │ Security Scan  │ │
│  └─────────┘    └──────────┘    └───────────┘    └────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│                         CD Pipeline (on tag)                          │
├──────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────────┐    ┌─────────────────────┐  │
│  │ Build Image │ -> │ Deploy Staging  │ -> │ Deploy Production   │  │
│  │ Push to     │    │ (auto)          │    │ (manual approval)   │  │
│  │ Registry    │    │ Run smoke tests │    │                     │  │
│  └─────────────┘    └─────────────────┘    └─────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

## Workflow Files

```
.github/workflows/
├── ci.yml         # Build, test, security scan (on push/PR)
├── cd.yml         # Docker build, staging, production (on tag)
└── pr-checks.yml  # Fast checks for pull requests
```

## Features

### CI (`ci.yml`)
- Java 21 + Maven with caching
- PostgreSQL & Redis service containers
- Integration tests against real services
- OWASP dependency vulnerability scan
- Test results and coverage reports

### CD (`cd.yml`)
- Multi-stage Docker build
- Push to GitHub Container Registry
- Staged deployment (staging -> production)
- Environment protection rules
- Smoke tests between stages

### PR Checks (`pr-checks.yml`)
- Code style validation
- Unit tests with published results
- Build verification
- JAR size check

## Usage

### Trigger CI

```bash
git push origin main
# or create a PR
```

### Trigger CD (Release)

```bash
git tag v1.0.0
git push origin v1.0.0
```

### Required Secrets

| Secret | Description |
|--------|-------------|
| `GITHUB_TOKEN` | Auto-provided for container registry |

### Required Environments

Create in GitHub repo settings:
- `staging` - Auto-deploy
- `production` - Require manual approval

## Docker Image

The Dockerfile uses:
- Multi-stage build (small final image)
- Layered JAR (optimal caching)
- Non-root user (security)
- Health checks

```bash
# Build locally
docker build -t myapp .

# Run
docker run -p 8080:8080 myapp
```

## Local Development

```bash
# Run tests
./mvnw test

# Build JAR
./mvnw package

# Run with Docker Compose (includes DB)
docker-compose up
```

## License

MIT
