# Spring AI + MCP Guide

Complete working example from [Spring AI + MCP: Build AI Agents That Actually Do Things](https://techyowls.io/blog/spring-ai-mcp-complete-guide).

## What's Inside

```
spring-ai-mcp-guide/
├── mcp-host/           # Main chatbot app (MCP Host)
├── mcp-server/         # Custom MCP Server with business logic
├── docker-compose.yml  # Run everything locally
└── README.md
```

## Quick Start

### Prerequisites

- Java 21+
- Node.js 18+ (for pre-built MCP servers)
- Docker (optional, for easy setup)

### Option 1: Docker Compose (Recommended)

```bash
# Set your API keys
export ANTHROPIC_API_KEY=your_key_here
export BRAVE_API_KEY=your_key_here  # Optional

# Start everything
docker-compose up -d

# Test it
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the latest news about Java?"}'
```

### Option 2: Manual Setup

**Terminal 1 - Start Custom MCP Server:**
```bash
cd mcp-server
./mvnw spring-boot:run
# Runs on port 8081
```

**Terminal 2 - Start MCP Host:**
```bash
cd mcp-host
export ANTHROPIC_API_KEY=your_key_here
./mvnw spring-boot:run
# Runs on port 8080
```

**Terminal 3 - Test:**
```bash
# Web search (requires Brave API key)
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Search for Spring AI news"}'

# Filesystem operation
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "List files in the data directory"}'

# Custom tool (product search)
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Search for laptop products"}'
```

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    MCP HOST (Port 8080)                     │
│  ┌─────────────┐                                            │
│  │   Claude    │                                            │
│  │   Sonnet    │                                            │
│  └──────┬──────┘                                            │
│         │                                                   │
│  ┌──────▼──────────────────────────────────────────────┐   │
│  │           MCP Client Manager                         │   │
│  │  ┌──────────┐ ┌──────────┐ ┌───────────────────┐    │   │
│  │  │  stdio   │ │  stdio   │ │       SSE         │    │   │
│  │  │ (brave)  │ │  (fs)    │ │ (custom-tools)    │    │   │
│  │  └────┬─────┘ └────┬─────┘ └─────────┬─────────┘    │   │
│  └───────┼────────────┼─────────────────┼──────────────┘   │
└──────────┼────────────┼─────────────────┼──────────────────┘
           │            │                 │
           ▼            ▼                 ▼
      [Brave API]  [./data]     ┌─────────────────────┐
                                │  MCP SERVER (8081)  │
                                │  ┌───────────────┐  │
                                │  │ ProductTools  │  │
                                │  │ OrderTools    │  │
                                │  └───────────────┘  │
                                └──────────┬──────────┘
                                           │
                                    [In-Memory DB]
```

## Available Tools

### Pre-built MCP Servers

| Server | Tools | Description |
|--------|-------|-------------|
| Brave Search | `brave_web_search` | Search the web for current information |
| Filesystem | `read_file`, `write_file`, `list_directory` | Local file operations |

### Custom MCP Server

| Tool | Description |
|------|-------------|
| `searchProducts` | Search products by name/category |
| `getProductDetails` | Get details for a specific product |
| `checkInventory` | Check stock levels |
| `placeOrder` | Create an order |

## Configuration

### MCP Host (`mcp-host/src/main/resources/application.yaml`)

```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      chat:
        options:
          model: claude-sonnet-4-20250514

    mcp:
      client:
        stdio:
          connections:
            brave-search:
              command: npx
              args: ["-y", "@modelcontextprotocol/server-brave-search"]
              env:
                BRAVE_API_KEY: ${BRAVE_API_KEY:}

            filesystem:
              command: npx
              args: ["-y", "@modelcontextprotocol/server-filesystem", "./data"]

        sse:
          connections:
            custom-tools:
              url: http://localhost:8081
```

### Custom MCP Server (`mcp-server/src/main/resources/application.yaml`)

```yaml
server:
  port: 8081

spring:
  ai:
    mcp:
      server:
        name: techyowls-product-tools
        version: 1.0.0
```

## Extending the Example

### Add a New Tool

1. Create a new class with `@Tool` annotated methods:

```java
public class WeatherTools {

    @Tool(description = "Get current weather for a city")
    public WeatherInfo getWeather(
            @ToolParam(description = "City name") String city) {
        // Implementation
    }
}
```

2. Register it:

```java
@Bean
ToolCallbackProvider weatherTools() {
    return MethodToolCallbackProvider.builder()
        .toolObjects(new WeatherTools())
        .build();
}
```

### Use a Different LLM

Replace the Anthropic starter with:

```xml
<!-- OpenAI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>

<!-- Or Ollama (local) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-ollama</artifactId>
</dependency>
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `npx` not found | Install Node.js 18+ |
| Connection refused | Ensure MCP server is running first |
| No tools registered | Check `application.yaml` MCP config |
| API key errors | Set environment variables |

## Learn More

- [Full Tutorial on TechyOwls](https://techyowls.io/blog/spring-ai-mcp-complete-guide)
- [MCP Specification](https://modelcontextprotocol.io)
- [Spring AI Docs](https://docs.spring.io/spring-ai/reference/)

## License

MIT - Use freely in your projects.
