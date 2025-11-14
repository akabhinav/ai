package com.worldclass.ai.core.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldclass.ai.core.LLMClientConfig;
import com.worldclass.ai.core.model.*;
import okhttp3.OkHttpClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TogetherAIAdapter - Implementation coming soon
 * Full production implementation includes request/response mapping,
 * error handling, retry logic, and provider-specific features
 */
public class TogetherAIAdapter implements LLMProviderAdapter {
    @Override
    public Mono<CompletionResponse> complete(CompletionRequest request, LLMClientConfig config, OkHttpClient httpClient, ObjectMapper objectMapper) {
        return Mono.error(new UnsupportedOperationException("TogetherAIAdapter - coming soon"));
    }

    @Override
    public Flux<CompletionResponse> completeStream(CompletionRequest request, LLMClientConfig config, OkHttpClient httpClient, ObjectMapper objectMapper) {
        return Flux.error(new UnsupportedOperationException("TogetherAIAdapter - coming soon"));
    }

    @Override
    public Mono<EmbeddingResponse> embed(EmbeddingRequest request, LLMClientConfig config, OkHttpClient httpClient, ObjectMapper objectMapper) {
        return Mono.error(new UnsupportedOperationException("TogetherAIAdapter - coming soon"));
    }

    @Override
    public int countTokens(String text, String model) {
        return text.length() / 4;
    }

    @Override
    public double calculateCost(int promptTokens, int completionTokens, String model) {
        return 0.0;
    }
}
