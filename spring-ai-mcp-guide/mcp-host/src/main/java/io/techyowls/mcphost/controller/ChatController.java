package io.techyowls.mcphost.controller;

import io.techyowls.mcphost.service.ChatbotService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * REST API for chatbot interactions.
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class ChatController {

    private final ChatbotService chatbotService;

    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * Synchronous chat endpoint.
     * POST /api/chat
     * Body: {"question": "your question here"}
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Question cannot be empty", false));
        }

        try {
            String answer = chatbotService.chat(request.question());
            return ResponseEntity.ok(new ChatResponse(answer, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ChatResponse("Error processing request: " + e.getMessage(), false));
        }
    }

    /**
     * Streaming chat endpoint.
     * POST /api/chat/stream
     * Returns Server-Sent Events for real-time response.
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return Flux.just("Error: Question cannot be empty");
        }

        return chatbotService.chatStream(request.question());
    }

    /**
     * Chat with context endpoint.
     * POST /api/chat/context
     * Useful for continuing conversations.
     */
    @PostMapping("/context")
    public ResponseEntity<ChatResponse> chatWithContext(@RequestBody ChatContextRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Question cannot be empty", false));
        }

        try {
            String answer = chatbotService.chatWithContext(
                    request.question(),
                    request.context() != null ? request.context() : ""
            );
            return ResponseEntity.ok(new ChatResponse(answer, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ChatResponse("Error processing request: " + e.getMessage(), false));
        }
    }

    // Request/Response DTOs

    public record ChatRequest(String question) {}

    public record ChatContextRequest(String question, String context) {}

    public record ChatResponse(String answer, boolean success) {
        public ChatResponse(String answer) {
            this(answer, true);
        }
    }
}
