package io.techyowls.springai.controller;

import io.techyowls.springai.model.RAGRequest;
import io.techyowls.springai.model.RAGResponse;
import io.techyowls.springai.service.RAGService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * REST controller for RAG operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RAGController {

    private final RAGService ragService;

    /**
     * Upload and ingest documents into vector store
     * 
     * Example:
     * POST /api/v1/rag/ingest
     * Content-Type: multipart/form-data
     * files: [file1.pdf, file2.txt]
     */
    @PostMapping("/ingest")
    public ResponseEntity<String> ingestDocuments(@RequestParam("files") List<MultipartFile> files) {
        log.info("Received {} files for ingestion", files.size());
        
        try {
            List<Resource> resources = files.stream()
                    .map(file -> {
                        try {
                            // Save temporarily and convert to Resource
                            Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
                            file.transferTo(tempFile.toFile());
                            return (Resource) new org.springframework.core.io.FileSystemResource(tempFile);
                        } catch (IOException e) {
                            log.error("Error processing file: {}", file.getOriginalFilename(), e);
                            return null;
                        }
                    })
                    .filter(resource -> resource != null)
                    .toList();
            
            ragService.ingestDocuments(resources);
            
            return ResponseEntity.ok("Successfully ingested " + resources.size() + " documents");
        } catch (Exception e) {
            log.error("Error ingesting documents", e);
            return ResponseEntity.internalServerError()
                    .body("Error ingesting documents: " + e.getMessage());
        }
    }

    /**
     * Query the RAG system
     * 
     * Example request:
     * POST /api/v1/rag/query
     * {
     *   "question": "What is Spring AI?",
     *   "topK": 5,
     *   "similarityThreshold": 0.7,
     *   "provider": "openai"
     * }
     */
    @PostMapping("/query")
    public ResponseEntity<RAGResponse> query(@Valid @RequestBody RAGRequest request) {
        log.info("Received RAG query: {}", request.getQuestion());
        RAGResponse response = ragService.query(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("RAG service is running");
    }
    
}
