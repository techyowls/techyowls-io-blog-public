package io.techyowls.springai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot AI Starter Application
 * 
 * Production-ready AI application with:
 * - Multi-provider support (OpenAI, Ollama, Azure)
 * - RAG (Retrieval Augmented Generation) system
 * - Vector store integration (pgvector)
 * - Function calling / Tools
 * - Image generation
 * - Observability and metrics
 */
@SpringBootApplication
public class SpringAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
    }

}
