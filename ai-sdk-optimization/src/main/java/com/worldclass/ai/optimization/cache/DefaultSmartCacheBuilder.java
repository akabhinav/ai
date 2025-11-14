package com.worldclass.ai.optimization.cache;

import com.worldclass.ai.core.LLMClient;

import java.time.Duration;

/**
 * Default implementation of SmartCacheBuilder
 */
public class DefaultSmartCacheBuilder implements SmartCacheBuilder {
    private long maxSize = 10000;
    private Duration ttl = Duration.ofHours(24);
    private double similarityThreshold = 0.95;
    private LLMClient embeddingClient;
    private String embeddingModel = "text-embedding-ada-002";
    private boolean useSemanticSimilarity = true;

    @Override
    public SmartCacheBuilder maxSize(long maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    @Override
    public SmartCacheBuilder ttl(Duration ttl) {
        this.ttl = ttl;
        return this;
    }

    @Override
    public SmartCacheBuilder similarityThreshold(double threshold) {
        this.similarityThreshold = threshold;
        return this;
    }

    @Override
    public SmartCacheBuilder embeddingClient(LLMClient client) {
        this.embeddingClient = client;
        return this;
    }

    @Override
    public SmartCacheBuilder embeddingModel(String model) {
        this.embeddingModel = model;
        return this;
    }

    @Override
    public SmartCacheBuilder useSemanticSimilarity(boolean use) {
        this.useSemanticSimilarity = use;
        return this;
    }

    @Override
    public SmartCache build() {
        return new DefaultSmartCache(
                maxSize,
                ttl,
                similarityThreshold,
                embeddingClient,
                embeddingModel,
                useSemanticSimilarity
        );
    }
}
