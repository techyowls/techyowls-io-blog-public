package io.techyowls.springai.controller;

import io.techyowls.springai.model.ChatRequest;
import io.techyowls.springai.model.ChatResponse;
import io.techyowls.springai.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for chat completions
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;


    /**
     * Simple chat endpoint
     * 
     * Example request:
     * POST /api/v1/chat
     * {
     *   "message": "Explain Spring AI in 3 sentences",
     *   "provider": "openai"
     * }
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request for provider: {}", request.getProvider());
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Stream chat endpoint (simplified for tutorial)
     */
    @PostMapping("/stream")
    public ResponseEntity<String> streamChat(@Valid @RequestBody ChatRequest request) {
        log.info("Received streaming chat request");
        String response = chatService.streamChat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chat service is running");
    }
    
}
