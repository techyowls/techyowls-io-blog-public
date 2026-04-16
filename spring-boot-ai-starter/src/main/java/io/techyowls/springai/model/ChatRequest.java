package io.techyowls.springai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request model for chat completions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    @NotBlank(message = "Message cannot be empty")
    private String message;
    
    /**
     * AI provider to use: "openai" or "ollama"
     */
    private String provider = "openai";
    
    /**
     * Model to use (optional, uses default from config if not specified)
     */
    private String model;
    
    /**
     * Temperature for randomness (0.0 to 1.0)
     */
    private Double temperature;
    
    /**
     * Maximum tokens to generate
     */
    private Integer maxTokens;
    
    /**
     * Additional options
     */
    private Map<String, Object> options;
    
}
