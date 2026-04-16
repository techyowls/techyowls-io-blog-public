package io.techyowls.springai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for RAG queries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RAGRequest {
    
    @NotBlank(message = "Question cannot be empty")
    private String question;
    
    /**
     * Number of similar documents to retrieve
     */
    private Integer topK = 5;
    
    /**
     * Minimum similarity threshold (0.0 to 1.0)
     */
    private Double similarityThreshold = 0.7;
    
    /**
     * AI provider to use for generation
     */
    private String provider = "openai";
    
}
