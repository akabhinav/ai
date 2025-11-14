package com.worldclass.ai.rag;

import com.worldclass.ai.core.LLMClient;

public class DefaultRAGEngineBuilder implements RAGEngineBuilder {
    private LLMClient llmClient;
    private LLMClient embeddingClient;
    private int chunkSize = 512;
    private int chunkOverlap = 128;
    private int topK = 5;
    private boolean useHybridSearch = true;
    private boolean useReranking = true;

    @Override
    public RAGEngineBuilder llmClient(LLMClient client) {
        this.llmClient = client;
        return this;
    }

    @Override
    public RAGEngineBuilder embeddingClient(LLMClient client) {
        this.embeddingClient = client;
        return this;
    }

    @Override
    public RAGEngineBuilder chunkSize(int size) {
        this.chunkSize = size;
        return this;
    }

    @Override
    public RAGEngineBuilder chunkOverlap(int overlap) {
        this.chunkOverlap = overlap;
        return this;
    }

    @Override
    public RAGEngineBuilder topK(int topK) {
        this.topK = topK;
        return this;
    }

    @Override
    public RAGEngineBuilder useHybridSearch(boolean use) {
        this.useHybridSearch = use;
        return this;
    }

    @Override
    public RAGEngineBuilder useReranking(boolean use) {
        this.useReranking = use;
        return this;
    }

    @Override
    public RAGEngine build() {
        return new DefaultRAGEngine(
                llmClient,
                embeddingClient != null ? embeddingClient : llmClient,
                chunkSize,
                chunkOverlap,
                topK,
                useHybridSearch,
                useReranking
        );
    }
}
