# Docker Compose Spring Boot Example

Code samples for: [Docker Compose for Spring Boot](https://techyowls.io/blog/docker-compose-spring-boot-complete-guide)

## Quick Start

```bash
docker compose up
```

Access at http://localhost:8080

## Services

| Service | Port | Purpose |
|---------|------|---------|
| app | 8080 | Spring Boot API |
| postgres | 5432 | PostgreSQL Database |
| redis | 6379 | Redis Cache |

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

## Development Mode

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up
```

## License

MIT
