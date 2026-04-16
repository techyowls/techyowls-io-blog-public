package io.techyowls.springai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response model for RAG queries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGResponse {
    
    private String answer;
    
    private List<RetrievedDocument> sources;
    
    private Integer documentsRetrieved;
    
    private String provider;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievedDocument {
        private String content;
        private Double similarityScore;
        private String source;
    }
    
}
