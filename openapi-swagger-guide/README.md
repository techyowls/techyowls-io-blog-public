# OpenAPI/Swagger with Spring Boot

Code samples for: [OpenAPI/Swagger Specification: Code-First vs Contract-First](https://techyowls.io/blog/openapi-swagger-spring-boot-guide)

## Two Approaches

```
Code-First (this demo):
┌────────────────┐     ┌────────────────┐     ┌────────────────┐
│ Java Code +    │ ──▶ │ OpenAPI Spec   │ ──▶ │  Swagger UI    │
│ Annotations    │     │ (generated)    │     │  + SDKs        │
└────────────────┘     └────────────────┘     └────────────────┘

Contract-First:
┌────────────────┐     ┌────────────────┐     ┌────────────────┐
│ OpenAPI YAML   │ ──▶ │ Generated Code │ ──▶ │ Implementation │
│ (design first) │     │ (interfaces)   │     │ (fill in logic)│
└────────────────┘     └────────────────┘     └────────────────┘
```

## Run

```bash
./mvnw spring-boot:run
```

Then visit:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

## Project Structure

```
src/main/java/io/techyowls/openapi/
├── config/
│   └── OpenApiConfig.java       # API info, security schemes
├── controller/
│   └── ProductController.java   # @Operation, @ApiResponses
├── model/
│   ├── Product.java             # @Schema annotations
│   ├── CreateProductRequest.java
│   └── PageResponse.java
└── exception/
    ├── ProductNotFoundException.java
    └── GlobalExceptionHandler.java
```

## Key Annotations

### API Metadata

```java
@OpenAPIDefinition(
    info = @Info(title = "Product API", version = "1.0.0"),
    servers = @Server(url = "http://localhost:8080")
)
@SecurityScheme(name = "bearerAuth", type = HTTP, scheme = "bearer")
```

### Endpoint Documentation

```java
@Operation(summary = "Get product by ID")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Found"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
@GetMapping("/{id}")
public Product getProduct(@PathVariable Long id) { ... }
```

### Model Documentation

```java
@Schema(description = "Product price", example = "29.99", minimum = "0.01")
@NotNull
private BigDecimal price;
```

## Generate Client SDKs

```bash
# Download OpenAPI spec
curl http://localhost:8080/v3/api-docs -o api.json

# Generate TypeScript client
npx @openapitools/openapi-generator-cli generate \
  -i api.json -g typescript-fetch -o ./client

# Generate Java client
npx @openapitools/openapi-generator-cli generate \
  -i api.json -g java -o ./java-client
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List products (paginated) |
| GET | `/api/products/{id}` | Get by ID |
| POST | `/api/products` | Create product |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |
| PATCH | `/api/products/{id}/stock` | Update stock |

## License

MIT
