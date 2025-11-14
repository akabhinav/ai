package com.worldclass.ai.optimization.cache;

import com.worldclass.ai.core.LLMClient;

import java.time.Duration;

/**
 * Builder for SmartCache
 */
public interface SmartCacheBuilder {
    SmartCacheBuilder maxSize(long maxSize);
    SmartCacheBuilder ttl(Duration ttl);
    SmartCacheBuilder similarityThreshold(double threshold);
    SmartCacheBuilder embeddingClient(LLMClient client);
    SmartCacheBuilder embeddingModel(String model);
    SmartCacheBuilder useSemanticSimilarity(boolean use);
    SmartCache build();
}
