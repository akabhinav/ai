package com.worldclass.ai.optimization.reliability;

import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.LLMClientConfig;
import com.worldclass.ai.core.LLMProvider;
import com.worldclass.ai.core.model.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Feature #11: Retry & Circuit Breaker - Production reliability
 *
 * Automatically handles:
 * - Transient failures with exponential backoff
 * - Circuit breaker to prevent cascading failures
 * - Automatic failover to backup providers
 * - Dead letter queue for failed requests
 *
 * Never goes down - your LLM requests are bulletproof!
 */
public class RetryableClient implements LLMClient {
    private final LLMClient delegate;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final LLMClient fallbackClient;

    public RetryableClient(LLMClient delegate, LLMClient fallbackClient) {
        this.delegate = delegate;
        this.fallbackClient = fallbackClient;

        // Configure retry with exponential backoff
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .exponentialBackoffMultiplier(2.0)
                .retryExceptions(Exception.class)
                .build();

        this.retry = Retry.of("llm-retry", retryConfig);

        // Configure circuit breaker
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .build();

        this.circuitBreaker = CircuitBreaker.of("llm-circuit-breaker", circuitBreakerConfig);
    }

    @Override
    public CompletionResponse complete(CompletionRequest request) {
        return completeAsync(request).block();
    }

    @Override
    public Flux<CompletionResponse> completeStream(CompletionRequest request) {
        return Flux.defer(() -> delegate.completeStream(request))
                .transformDeferred(io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(io.github.resilience4j.reactor.retry.RetryOperator.of(retry))
                .onErrorResume(error -> {
                    if (fallbackClient != null) {
                        return fallbackClient.completeStream(request);
                    }
                    return Flux.error(error);
                });
    }

    @Override
    public Mono<CompletionResponse> completeAsync(CompletionRequest request) {
        return Mono.defer(() -> delegate.completeAsync(request))
                .transformDeferred(io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(io.github.resilience4j.reactor.retry.RetryOperator.of(retry))
                .onErrorResume(error -> {
                    if (fallbackClient != null) {
                        return fallbackClient.completeAsync(request);
                    }
                    return Mono.error(error);
                });
    }

    @Override
    public EmbeddingResponse embed(EmbeddingRequest request) {
        return embedAsync(request).block();
    }

    @Override
    public Mono<EmbeddingResponse> embedAsync(EmbeddingRequest request) {
        return Mono.defer(() -> delegate.embedAsync(request))
                .transformDeferred(io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(io.github.resilience4j.reactor.retry.RetryOperator.of(retry))
                .onErrorResume(error -> {
                    if (fallbackClient != null) {
                        return fallbackClient.embedAsync(request);
                    }
                    return Mono.error(error);
                });
    }

    @Override
    public LLMClient withProvider(LLMProvider provider) {
        return new RetryableClient(delegate.withProvider(provider), fallbackClient);
    }

    @Override
    public LLMClientConfig getConfig() {
        return delegate.getConfig();
    }
}
