package io.techyowls.springai.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.techyowls.springai.model.ChatRequest;
import io.techyowls.springai.model.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service for handling chat completions with multiple AI providers
 */
@Slf4j
@Service
public class ChatService {

    private final OpenAiChatModel openAiChatModel;
    private final OllamaChatModel ollamaChatModel;
    private final Counter chatRequestCounter;
    private final Timer chatResponseTimer;

    public ChatService(
            @Qualifier("openAiChatModel") OpenAiChatModel openAiChatModel,
            @Qualifier("ollamaChatModel") OllamaChatModel ollamaChatModel,
            MeterRegistry meterRegistry) {
        this.openAiChatModel = openAiChatModel;
        this.ollamaChatModel = ollamaChatModel;
        
        // Initialize metrics
        this.chatRequestCounter = Counter.builder("chat.requests.total")
                .description("Total number of chat requests")
                .register(meterRegistry);
        this.chatResponseTimer = Timer.builder("chat.response.time")
                .description("Chat response time")
                .register(meterRegistry);
    }

    /**
     * Process a chat request using the specified provider
     */
    public ChatResponse chat(ChatRequest request) {
        chatRequestCounter.increment();
        
        return chatResponseTimer.record(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                log.info("Processing chat request with provider: {}", request.getProvider());
                
                ChatModel chatModel = selectChatModel(request.getProvider());
                Prompt prompt = new Prompt(request.getMessage());
                
                String response = chatModel.call(prompt).getResult().getOutput().getContent();
                
                long endTime = System.currentTimeMillis();
                
                return ChatResponse.builder()
                        .response(response)
                        .provider(request.getProvider())
                        .model(request.getModel())
                        .processingTimeMs((double) (endTime - startTime))
                        .timestamp(Instant.now())
                        .build();
                        
            } catch (Exception e) {
                log.error("Error processing chat request: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process chat request: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Stream chat responses (for real-time streaming)
     */
    public String streamChat(ChatRequest request) {
        log.info("Streaming chat with provider: {}", request.getProvider());
        ChatModel chatModel = selectChatModel(request.getProvider());
        
        // For streaming, we'd use Flux<String> in a real implementation
        // This is simplified for the tutorial
        return chatModel.call(new Prompt(request.getMessage()))
                .getResult()
                .getOutput()
                .getContent();
    }

    /**
     * Select the appropriate chat model based on provider
     */
    private ChatModel selectChatModel(String provider) {
        return switch (provider.toLowerCase()) {
            case "openai" -> openAiChatModel;
            case "ollama" -> ollamaChatModel;
            default -> throw new IllegalArgumentException(
                    "Unknown provider: " + provider + ". Supported providers: openai, ollama"
            );
        };
    }
    
}
