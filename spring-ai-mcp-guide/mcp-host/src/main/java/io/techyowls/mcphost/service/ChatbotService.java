package io.techyowls.mcphost.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Service layer for chatbot interactions.
 * Handles both synchronous and streaming responses.
 */
@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private final ChatClient chatClient;

    public ChatbotService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Process a chat message and return the complete response.
     * The LLM will automatically use MCP tools when appropriate.
     *
     * @param question The user's question
     * @return The AI's response
     */
    public String chat(String question) {
        log.debug("Processing question: {}", question);

        String response = chatClient
                .prompt()
                .user(question)
                .call()
                .content();

        log.debug("Response generated: {} chars", response.length());
        return response;
    }

    /**
     * Process a chat message and stream the response.
     * Better for long responses - shows text as it's generated.
     *
     * @param question The user's question
     * @return A stream of response chunks
     */
    public Flux<String> chatStream(String question) {
        log.debug("Processing streaming question: {}", question);

        return chatClient
                .prompt()
                .user(question)
                .stream()
                .content();
    }

    /**
     * Process a chat message with conversation history context.
     * Useful for multi-turn conversations.
     *
     * @param question The user's question
     * @param context  Previous conversation context
     * @return The AI's response
     */
    public String chatWithContext(String question, String context) {
        log.debug("Processing question with context");

        return chatClient
                .prompt()
                .system(s -> s.text("""
                        Previous conversation context:
                        {context}

                        Continue the conversation based on this context.
                        """).param("context", context))
                .user(question)
                .call()
                .content();
    }
}
