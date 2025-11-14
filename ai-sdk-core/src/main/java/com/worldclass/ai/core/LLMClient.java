package com.worldclass.ai.core;

import com.worldclass.ai.core.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Feature #1: Universal LLM Client - 12 providers in one API
 *
 * This is the main interface for interacting with any LLM provider.
 * Switch providers with a single line of code - no vendor lock-in!
 *
 * Example:
 * <pre>
 * LLMClient client = LLMClient.builder()
 *     .provider(LLMProvider.OPENAI)
 *     .apiKey("sk-...")
 *     .build();
 *
 * // Switch to Anthropic with one line
 * client = client.withProvider(LLMProvider.ANTHROPIC);
 * </pre>
 */
public interface LLMClient {

    /**
     * Synchronous completion
     */
    CompletionResponse complete(CompletionRequest request);

    /**
     * Feature #2: Streaming Responses - Real-time token delivery
     * Returns a reactive stream of tokens as they arrive
     */
    Flux<CompletionResponse> completeStream(CompletionRequest request);

    /**
     * Async completion using Project Reactor
     */
    Mono<CompletionResponse> completeAsync(CompletionRequest request);

    /**
     * Feature #5: Embeddings & Vectors - Semantic search
     */
    EmbeddingResponse embed(EmbeddingRequest request);

    /**
     * Async embeddings
     */
    Mono<EmbeddingResponse> embedAsync(EmbeddingRequest request);

    /**
     * Switch provider (for multi-provider failover)
     */
    LLMClient withProvider(LLMProvider provider);

    /**
     * Get current configuration
     */
    LLMClientConfig getConfig();

    /**
     * Builder pattern for client creation
     */
    static LLMClientBuilder builder() {
        return new DefaultLLMClientBuilder();
    }
}
