# Spring Security JWT

Code samples for: [Spring Security JWT Authentication](https://techyowls.io/blog/spring-security-jwt-complete-guide)

## Run

```bash
./mvnw spring-boot:run
```

## Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | /api/auth/login | No | Get tokens |
| POST | /api/auth/refresh | No | Refresh access token |
| GET | /api/public/** | No | Public endpoints |
| GET | /api/admin/** | ADMIN role | Admin only |
| * | /** | Yes | All other endpoints |

## Usage

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}'

# Use token
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <access_token>"

# Refresh
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh_token>"}'
```

## Configuration

Set `JWT_SECRET` environment variable in production:

```bash
export JWT_SECRET=$(openssl rand -base64 64)
```

## License

MIT
