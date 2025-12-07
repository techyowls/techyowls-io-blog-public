# Spring AI Structured Output

Code samples for the TechyOwls tutorial: [Spring AI Structured Output: Parse LLM Responses into Java Objects](https://techyowls.io/blog/spring-ai-structured-output-json)

## Prerequisites

- Java 21+
- Maven 3.8+
- OpenAI API key

## Setup

```bash
export OPENAI_API_KEY=your_key_here
```

## Run

```bash
./mvnw spring-boot:run
```

## API Examples

### Generate a D&D Character

```bash
curl http://localhost:8080/api/character?race=Dwarf
```

Response:
```json
{
  "name": "Thorin Ironforge",
  "age": 156,
  "race": "Dwarf",
  "characterClass": "Wizard",
  "bio": "Thorin discovered magic in the deep mines of his homeland..."
}
```

### Generate a Party

```bash
curl http://localhost:8080/api/party?count=4
```

### Generate Products

```bash
curl "http://localhost:8080/api/products?category=Electronics&count=3"
```

Response:
```json
[
  {
    "sku": "ELEC-12345",
    "name": "UltraSound Pro Earbuds",
    "description": "Wireless earbuds with active noise cancellation.",
    "price": 149.99,
    "category": "Electronics",
    "tags": ["audio", "wireless", "bluetooth", "noise-cancelling"]
  }
]
```

### Generate Single Product

```bash
curl -X POST http://localhost:8080/api/product \
  -H "Content-Type: text/plain" \
  -d "A portable Bluetooth speaker for outdoor use"
```

## Key Classes

| Class | Purpose |
|-------|---------|
| `Character` | D&D character record |
| `Product` | Product with validation |
| `GenericMapOutputConverter` | Custom converter for typed maps |
| `CharacterGeneratorService` | Generate characters |
| `ProductCatalogService` | Generate product catalogs |

## Converters Used

| Converter | Output | Example |
|-----------|--------|---------|
| `BeanOutputConverter` | Single object | `Character`, `Product` |
| `ParameterizedTypeReference<List<T>>` | List of objects | `List<Character>` |
| `GenericMapOutputConverter` | Typed map | `Map<String, Character>` |

## License

MIT
