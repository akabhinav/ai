package com.worldclass.ai.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.worldclass.ai.core.model.*;
import com.worldclass.ai.core.provider.LLMProviderAdapter;
import com.worldclass.ai.core.provider.ProviderAdapterFactory;
import okhttp3.OkHttpClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * Default implementation of Universal LLM Client
 * Feature #1: Universal LLM Client - 12 providers in one API
 */
public class DefaultLLMClient implements LLMClient {
    private final LLMClientConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final LLMProviderAdapter providerAdapter;

    public DefaultLLMClient(LLMClientConfig config) {
        this.config = config;
        this.httpClient = createHttpClient(config);
        this.objectMapper = createObjectMapper();
        this.providerAdapter = ProviderAdapterFactory.create(config.getProvider());
    }

    @Override
    public CompletionResponse complete(CompletionRequest request) {
        return completeAsync(request).block();
    }

    @Override
    public Flux<CompletionResponse> completeStream(CompletionRequest request) {
        return providerAdapter.completeStream(request, config, httpClient, objectMapper);
    }

    @Override
    public Mono<CompletionResponse> completeAsync(CompletionRequest request) {
        return providerAdapter.complete(request, config, httpClient, objectMapper);
    }

    @Override
    public EmbeddingResponse embed(EmbeddingRequest request) {
        return embedAsync(request).block();
    }

    @Override
    public Mono<EmbeddingResponse> embedAsync(EmbeddingRequest request) {
        return providerAdapter.embed(request, config, httpClient, objectMapper);
    }

    @Override
    public LLMClient withProvider(LLMProvider provider) {
        LLMClientConfig newConfig = LLMClientConfig.builder()
                .provider(provider)
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .timeout(config.getTimeout())
                .connectTimeout(config.getConnectTimeout())
                .maxRetries(config.getMaxRetries())
                .headers(config.getHeaders())
                .build();
        return new DefaultLLMClient(newConfig);
    }

    @Override
    public LLMClientConfig getConfig() {
        return config;
    }

    private OkHttpClient createHttpClient(LLMClientConfig config) {
        return new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.getTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
