package io.techyowls.springai.service;

import io.techyowls.springai.model.RAGRequest;
import io.techyowls.springai.model.RAGResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for Retrieval Augmented Generation (RAG)
 * Combines document retrieval with AI generation for context-aware responses
 */
@Slf4j
@Service
public class RAGService {

    private final VectorStore vectorStore;
    private final OpenAiChatModel openAiChatModel;
    private final OllamaChatModel ollamaChatModel;
    private final TextSplitter textSplitter;

    private static final String RAG_PROMPT_TEMPLATE = """
            You are a helpful assistant. Answer the user's question based on the following context.
            If the answer cannot be found in the context, say so.
            
            Context:
            {context}
            
            Question: {question}
            
            Answer:
            """;

    public RAGService(
            VectorStore vectorStore,
            @Qualifier("openAiChatModel") OpenAiChatModel openAiChatModel,
            @Qualifier("ollamaChatModel") OllamaChatModel ollamaChatModel) {
        this.vectorStore = vectorStore;
        this.openAiChatModel = openAiChatModel;
        this.ollamaChatModel = ollamaChatModel;
        this.textSplitter = new TokenTextSplitter();
    }

    /**
     * Ingest documents into the vector store
     */
    public void ingestDocuments(List<Resource> resources) {
        log.info("Ingesting {} documents", resources.size());
        
        List<Document> allDocuments = resources.stream()
                .flatMap(resource -> {
                    try {
                        // Handle different file types
                        if (resource.getFilename() != null && resource.getFilename().endsWith(".pdf")) {
                            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
                            return pdfReader.get().stream();
                        } else {
                            TextReader textReader = new TextReader(resource);
                            return textReader.get().stream();
                        }
                    } catch (Exception e) {
                        log.error("Error reading document: {}", resource.getFilename(), e);
                        return List.<Document>of().stream();
                    }
                })
                .toList();

        // Split documents into chunks
        List<Document> chunks = textSplitter.apply(allDocuments);
        
        // Add to vector store
        vectorStore.add(chunks);
        
        log.info("Successfully ingested {} document chunks", chunks.size());
    }

    /**
     * Query the RAG system with a question
     */
    public RAGResponse query(RAGRequest request) {
        log.info("Processing RAG query: {}", request.getQuestion());
        
        // Step 1: Retrieve relevant documents
        SearchRequest searchRequest = SearchRequest.query(request.getQuestion())
                .withTopK(request.getTopK())
                .withSimilarityThreshold(request.getSimilarityThreshold());
        
        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
        
        if (similarDocuments.isEmpty()) {
            log.warn("No relevant documents found for query: {}", request.getQuestion());
            return RAGResponse.builder()
                    .answer("I couldn't find any relevant information to answer your question.")
                    .sources(List.of())
                    .documentsRetrieved(0)
                    .provider(request.getProvider())
                    .build();
        }
        
        // Step 2: Build context from retrieved documents
        String context = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n"));
        
        // Step 3: Generate answer using AI with context
        PromptTemplate promptTemplate = new PromptTemplate(RAG_PROMPT_TEMPLATE);
        Prompt prompt = promptTemplate.create(Map.of(
                "context", context,
                "question", request.getQuestion()
        ));
        
        ChatModel chatModel = selectChatModel(request.getProvider());
        String answer = chatModel.call(prompt).getResult().getOutput().getContent();
        
        // Step 4: Build response with sources
        List<RAGResponse.RetrievedDocument> sources = similarDocuments.stream()
                .map(doc -> RAGResponse.RetrievedDocument.builder()
                        .content(doc.getContent().substring(0, Math.min(200, doc.getContent().length())) + "...")
                        .source(doc.getMetadata().getOrDefault("source", "Unknown").toString())
                        .similarityScore(null) // Spring AI doesn't expose scores yet
                        .build())
                .toList();
        
        return RAGResponse.builder()
                .answer(answer)
                .sources(sources)
                .documentsRetrieved(similarDocuments.size())
                .provider(request.getProvider())
                .build();
    }

    /**
     * Select the appropriate chat model based on provider
     */
    private ChatModel selectChatModel(String provider) {
        return switch (provider.toLowerCase()) {
            case "openai" -> openAiChatModel;
            case "ollama" -> ollamaChatModel;
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }
    
}
