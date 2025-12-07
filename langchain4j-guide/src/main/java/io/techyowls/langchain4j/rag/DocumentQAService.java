package io.techyowls.langchain4j.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval Augmented Generation) - answer questions from your documents.
 *
 * This is the core value of LangChain4j - grounding LLM responses in YOUR data.
 */
public class DocumentQAService {

    private final ChatLanguageModel model;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> store;

    public DocumentQAService() {
        this.model = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName("gpt-4o")
            .build();

        // Local embedding model - no API calls, runs on CPU
        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        // In-memory vector store (use Pinecone, Chroma, etc. in production)
        this.store = new InMemoryEmbeddingStore<>();
    }

    /**
     * Index a document for later retrieval.
     */
    public void indexDocument(Path filePath) {
        System.out.println("Indexing: " + filePath);

        // Load document
        Document doc = FileSystemDocumentLoader.loadDocument(filePath);

        // Split into chunks (500 tokens, 100 overlap)
        var splitter = DocumentSplitters.recursive(500, 100);
        List<TextSegment> segments = splitter.split(doc);
        System.out.println("Split into " + segments.size() + " segments");

        // Create embeddings
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        // Store in vector database
        store.addAll(embeddings, segments);
        System.out.println("Indexed successfully!");
    }

    /**
     * Ask a question - retrieves relevant context and generates answer.
     */
    public String askQuestion(String question) {
        // Create embedding for the question
        Embedding questionEmbedding = embeddingModel.embed(question).content();

        // Find relevant document segments
        List<EmbeddingMatch<TextSegment>> matches = store.findRelevant(
            questionEmbedding,
            5,    // top 5 results
            0.6   // minimum similarity score
        );

        if (matches.isEmpty()) {
            return "I couldn't find relevant information in the documents.";
        }

        // Build context from matches
        String context = matches.stream()
            .map(m -> m.embedded().text())
            .collect(Collectors.joining("\n---\n"));

        // Generate answer with context
        String prompt = """
            You are a helpful assistant. Answer the question based ONLY on
            the provided context. If the answer isn't in the context, say so.

            Context:
            %s

            Question: %s

            Answer:
            """.formatted(context, question);

        return model.generate(prompt);
    }

    public static void main(String[] args) {
        DocumentQAService service = new DocumentQAService();

        // Index sample documents (create a sample.txt in resources/)
        // service.indexDocument(Path.of("src/main/resources/sample.txt"));

        // Ask questions
        // String answer = service.askQuestion("What is the refund policy?");
        // System.out.println(answer);

        System.out.println("DocumentQAService ready. Index documents and ask questions!");
    }
}
