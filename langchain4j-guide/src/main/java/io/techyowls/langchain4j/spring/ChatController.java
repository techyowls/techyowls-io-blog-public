package io.techyowls.langchain4j.spring;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller using auto-injected ChatLanguageModel.
 *
 * Spring Boot starter auto-configures the model from application.yaml
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatLanguageModel model;

    public ChatController(ChatLanguageModel model) {
        this.model = model;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = model.generate(request.message());
        return new ChatResponse(answer);
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    public record ChatRequest(String message) {}
    public record ChatResponse(String answer) {}
}
