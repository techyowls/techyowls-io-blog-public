# REST API Best Practices

Code samples for: [REST API Best Practices in Java](https://techyowls.io/blog/rest-api-best-practices-java)

## Run

```bash
./mvnw spring-boot:run
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/users | List users (paginated) |
| GET | /api/v1/users/{id} | Get user |
| POST | /api/v1/users | Create user |
| PUT | /api/v1/users/{id} | Full update |
| PATCH | /api/v1/users/{id} | Partial update |
| DELETE | /api/v1/users/{id} | Delete user |

## Examples

```bash
# Create
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","age":25}'

# List
curl http://localhost:8080/api/v1/users?page=0&size=10

# Get one
curl http://localhost:8080/api/v1/users/1
```

## License

MIT
