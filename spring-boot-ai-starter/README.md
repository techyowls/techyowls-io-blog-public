# Spring Boot 3 + Spring AI Starter

A production-ready Spring Boot 3 application demonstrating AI integration with multiple providers, RAG (Retrieval Augmented Generation), vector stores, and comprehensive observability.

## 🚀 Features

- **Multi-Provider AI Support**: OpenAI and Ollama (local) with easy provider switching
- **RAG System**: Document ingestion, vector storage, and context-aware generation
- **Vector Store**: PostgreSQL with pgvector for semantic search
- **Production-Ready**: Metrics, tracing, error handling, and health checks
- **Docker Support**: One-command setup with Docker Compose
- **Comprehensive APIs**: RESTful endpoints for chat and RAG operations

## 📋 Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- OpenAI API key (optional, for OpenAI provider)

## 🏗️ Quick Start

### 1. Clone and Setup

```bash
git clone https://github.com/your-username/spring-boot-ai-starter.git
cd spring-boot-ai-starter
```

### 2. Configure Environment

Create `.env` file or set environment variables:

```bash
export OPENAI_API_KEY=sk-your-openai-api-key
```

### 3. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- PostgreSQL with pgvector extension (port 5432)
- Ollama for local AI (port 11434)

### 4. Pull Ollama Models (Optional)

```bash
docker exec -it spring-ai-ollama ollama pull llama3.2
docker exec -it spring-ai-ollama ollama pull nomic-embed-text
```

### 5. Run the Application

```bash
./mvnw spring-boot:run
```

The application will be available at `http://localhost:8080`

## 📚 API Examples

### Chat Completion

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Explain Spring AI in 3 sentences",
    "provider": "openai"
  }'
```

**Response:**
```json
{
  "response": "Spring AI is a framework that provides abstractions for AI services...",
  "provider": "openai",
  "model": "gpt-4o-mini",
  "processingTimeMs": 1234.56,
  "timestamp": "2024-12-07T10:30:00Z"
}
```

### RAG - Ingest Documents

```bash
curl -X POST http://localhost:8080/api/v1/rag/ingest \
  -F "files=@document1.pdf" \
  -F "files=@document2.txt"
```

### RAG - Query

```bash
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the main topic of the documents?",
    "topK": 5,
    "similarityThreshold": 0.7,
    "provider": "openai"
  }'
```

**Response:**
```json
{
  "answer": "Based on the provided documents, the main topic is...",
  "sources": [
    {
      "content": "Document excerpt...",
      "source": "document1.pdf",
      "similarityScore": 0.89
    }
  ],
  "documentsRetrieved": 5,
  "provider": "openai"
}
```

## 🏗️ Architecture

```
┌─────────────────┐
│  REST API       │
│  Controllers    │
└────────┬────────┘
         │
┌────────▼────────┐
│  Services       │
│  - ChatService  │
│  - RAGService   │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼──┐  ┌──▼────┐
│OpenAI│  │Ollama │
└──────┘  └───────┘
    │
┌───▼──────────┐
│ Vector Store │
│  (pgvector)  │
└──────────────┘
```

## 📊 Monitoring

- **Health**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`

## 🧪 Testing

```bash
./mvnw test
```

## 🔧 Configuration

Key configuration in `application.yml`:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
    
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.2
```

## 📖 Learn More

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Full Tutorial on TechyOwls Blog](https://techyowls.io/blog/spring-boot-ai-complete-guide)

## 🤝 Contributing

Contributions welcome! Please read our contributing guidelines.

## 📄 License

MIT License - see LICENSE file for details

## 🙋 Support

- GitHub Issues: [Report bugs](https://github.com/your-username/spring-boot-ai-starter/issues)
- Blog: [techyowls.io](https://techyowls.io)
