package com.worldclass.ai.rag;

import com.worldclass.ai.core.LLMClient;

/**
 * Builder for RAG Engine
 */
public interface RAGEngineBuilder {
    RAGEngineBuilder llmClient(LLMClient client);
    RAGEngineBuilder embeddingClient(LLMClient client);
    RAGEngineBuilder chunkSize(int size);
    RAGEngineBuilder chunkOverlap(int overlap);
    RAGEngineBuilder topK(int topK);
    RAGEngineBuilder useHybridSearch(boolean use);
    RAGEngineBuilder useReranking(boolean use);
    RAGEngine build();
}
