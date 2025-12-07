package io.techyowls.mcphost.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the ChatClient with MCP tool integration.
 */
@Configuration
public class ChatConfig {

    private static final Logger log = LoggerFactory.getLogger(ChatConfig.class);

    private static final String SYSTEM_PROMPT = """
            You are a helpful AI assistant with access to external tools.

            Available capabilities:
            - Web search: Use when asked about current events, news, or real-time information
            - Filesystem: Use when asked to read, write, or list files in the data directory
            - Product tools: Use when asked about products, inventory, or orders

            Guidelines:
            - Always cite sources when using web search results
            - Be concise but thorough in your responses
            - If a tool fails, explain what happened and suggest alternatives
            - Never make up information - use tools to verify facts
            """;

    /**
     * Creates the ChatClient with all registered MCP tools.
     */
    @Bean
    ChatClient chatClient(ChatModel chatModel, SyncMcpToolCallbackProvider toolCallbackProvider) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();
    }

    /**
     * Logs all registered MCP tools on startup for debugging.
     */
    @Bean
    ApplicationRunner mcpToolsDebugger(SyncMcpToolCallbackProvider toolCallbackProvider) {
        return args -> {
            var tools = toolCallbackProvider.getToolCallbacks();
            log.info("=== Registered MCP Tools ({}) ===", tools.size());
            tools.forEach(tool -> log.info("  - {}", tool.getName()));
            log.info("================================");
        };
    }
}
