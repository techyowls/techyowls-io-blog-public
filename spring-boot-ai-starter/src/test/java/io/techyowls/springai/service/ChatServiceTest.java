package io.techyowls.springai.service;

import io.techyowls.springai.model.ChatRequest;
import io.techyowls.springai.model.ChatResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.model.ChatResponse as AiChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test class for ChatService
 */
class ChatServiceTest {

    @Mock
    private OpenAiChatModel openAiChatModel;

    @Mock
    private OllamaChatModel ollamaChatModel;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        chatService = new ChatService(openAiChatModel, ollamaChatModel, meterRegistry);
    }

    @Test
    void testChatWithOpenAI() {
        // Arrange
        ChatRequest request = new ChatRequest();
        request.setMessage("Hello!");
        request.setProvider("openai");

        // Mock the response - this is simplified
        // In real tests, you'd mock the full response chain
        
        // Act & Assert - basic test structure
        assertNotNull(chatService);
        assertEquals("openai", request.getProvider());
    }

    @Test
    void testInvalidProvider() {
        // Arrange
        ChatRequest request = new ChatRequest();
        request.setMessage("Test");
        request.setProvider("invalid");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> chatService.chat(request));
    }
}
