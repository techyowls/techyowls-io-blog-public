package io.techyowls.mcpserver.config;

import io.techyowls.mcpserver.tools.ProductTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server configuration.
 * Registers tools that will be exposed to MCP clients.
 */
@Configuration
public class McpServerConfig {

    private static final Logger log = LoggerFactory.getLogger(McpServerConfig.class);

    /**
     * Register product tools with the MCP server.
     * All methods annotated with @Tool in ProductTools will be exposed.
     */
    @Bean
    ToolCallbackProvider productTools() {
        return MethodToolCallbackProvider.builder()
                .toolObjects(new ProductTools())
                .build();
    }

    /**
     * Log registered tools on startup.
     */
    @Bean
    ApplicationRunner toolsLogger(ToolCallbackProvider toolCallbackProvider) {
        return args -> {
            log.info("=== MCP Server Tools Registered ===");
            toolCallbackProvider.getToolCallbacks()
                    .forEach(tool -> log.info("  - {} : {}",
                            tool.getName(),
                            tool.getDescription()));
            log.info("===================================");
        };
    }
}
