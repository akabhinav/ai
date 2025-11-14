package com.worldclass.ai.core.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldclass.ai.core.LLMClientConfig;
import com.worldclass.ai.core.model.*;
import okhttp3.OkHttpClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adapter interface for different LLM providers
 * Each provider has its own implementation
 */
public interface LLMProviderAdapter {

    /**
     * Complete a request asynchronously
     */
    Mono<CompletionResponse> complete(
            CompletionRequest request,
            LLMClientConfig config,
            OkHttpClient httpClient,
            ObjectMapper objectMapper
    );

    /**
     * Feature #2: Streaming Responses - Real-time token delivery
     */
    Flux<CompletionResponse> completeStream(
            CompletionRequest request,
            LLMClientConfig config,
            OkHttpClient httpClient,
            ObjectMapper objectMapper
    );

    /**
     * Feature #5: Embeddings & Vectors
     */
    Mono<EmbeddingResponse> embed(
            EmbeddingRequest request,
            LLMClientConfig config,
            OkHttpClient httpClient,
            ObjectMapper objectMapper
    );

    /**
     * Calculate token count (provider-specific)
     */
    int countTokens(String text, String model);

    /**
     * Calculate cost for this provider
     */
    double calculateCost(int promptTokens, int completionTokens, String model);
}
