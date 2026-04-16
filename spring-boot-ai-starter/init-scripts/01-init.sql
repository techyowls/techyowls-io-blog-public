-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create vector store table (Spring AI will manage this, but we create it here for reference)
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT NOT NULL,
    metadata JSONB,
    embedding vector(1536)
);

-- Create index for faster similarity search
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx 
    ON vector_store 
    USING hnsw (embedding vector_cosine_ops);
