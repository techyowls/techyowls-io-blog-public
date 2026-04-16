package io.techyowls.springai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response model for chat completions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    private String response;
    
    private String provider;
    
    private String model;
    
    private Integer tokensUsed;
    
    private Double processingTimeMs;
    
    private Instant timestamp;
    
}
