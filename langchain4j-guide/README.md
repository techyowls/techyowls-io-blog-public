# LangChain4j Guide

Code samples for the TechyOwls tutorial: [LangChain4j: Build AI Apps in Java](https://techyowls.io/blog/langchain4j-java-complete-guide)

## Prerequisites

- Java 21+
- Maven 3.8+
- OpenAI API key

## Setup

```bash
export OPENAI_API_KEY=your_key_here
```

## Run Examples

### Basic Chat

```bash
./mvnw compile exec:java -Dexec.mainClass="io.techyowls.langchain4j.basic.BasicChatExample"
```

### Prompt Templates

```bash
./mvnw compile exec:java -Dexec.mainClass="io.techyowls.langchain4j.basic.PromptTemplateExample"
```

### Chat Memory

```bash
./mvnw compile exec:java -Dexec.mainClass="io.techyowls.langchain4j.memory.ChatMemoryExample"
```

### Agent with Tools

```bash
./mvnw compile exec:java -Dexec.mainClass="io.techyowls.langchain4j.agents.AgentExample"
```

### Spring Boot App

```bash
./mvnw spring-boot:run
```

Then:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain virtual threads in 2 sentences"}'
```

## Key Classes

| Package | Class | Purpose |
|---------|-------|---------|
| `basic` | `BasicChatExample` | Simple 3-line AI chat |
| `basic` | `PromptTemplateExample` | Structured prompts |
| `memory` | `ChatMemoryExample` | Conversation history |
| `rag` | `DocumentQAService` | RAG with embeddings |
| `agents` | `AgentExample` | LLM with tools |
| `spring` | `ChatController` | REST API |

## Project Structure

```
langchain4j-guide/
├── src/main/java/io/techyowls/langchain4j/
│   ├── basic/          # Simple examples
│   ├── memory/         # Chat memory
│   ├── rag/            # Document Q&A
│   ├── agents/         # Tools and agents
│   └── spring/         # Spring Boot integration
└── pom.xml
```

## License

MIT
