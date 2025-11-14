package com.worldclass.ai.optimization.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.model.CompletionRequest;
import com.worldclass.ai.core.model.CompletionResponse;
import com.worldclass.ai.core.model.EmbeddingRequest;
import com.worldclass.ai.core.model.EmbeddingResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Feature #9: Smart Caching with semantic similarity
 *
 * Implementation uses:
 * 1. Caffeine cache for fast in-memory storage
 * 2. Embeddings for semantic similarity matching
 * 3. Cosine similarity to find similar requests
 */
public class DefaultSmartCache implements SmartCache {
    private final Cache<String, CachedEntry> cache;
    private final Map<String, List<Double>> embeddings = new ConcurrentHashMap<>();
    private final double similarityThreshold;
    private final LLMClient embeddingClient;
    private final String embeddingModel;
    private final boolean useSemanticSimilarity;

    // Statistics
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong totalSavings = new AtomicLong(0);

    public DefaultSmartCache(
            long maxSize,
            Duration ttl,
            double similarityThreshold,
            LLMClient embeddingClient,
            String embeddingModel,
            boolean useSemanticSimilarity
    ) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();

        this.similarityThreshold = similarityThreshold;
        this.embeddingClient = embeddingClient;
        this.embeddingModel = embeddingModel;
        this.useSemanticSimilarity = useSemanticSimilarity;
    }

    @Override
    public Optional<CachedResponse> get(CompletionRequest request) {
        String key = createKey(request);

        // Try exact match first
        CachedEntry entry = cache.getIfPresent(key);
        if (entry != null) {
            hits.incrementAndGet();
            totalSavings.addAndGet((long) (entry.response.getUsage().getEstimatedCost() * 100));

            return Optional.of(CachedResponse.builder()
                    .response(entry.response)
                    .similarityScore(1.0)
                    .cachedAt(entry.timestamp)
                    .cacheKey(key)
                    .build());
        }

        // Try semantic similarity match if enabled
        if (useSemanticSimilarity && embeddingClient != null) {
            Optional<CachedResponse> similarMatch = findSimilarMatch(request, key);
            if (similarMatch.isPresent()) {
                hits.incrementAndGet();
                CachedEntry matchEntry = cache.getIfPresent(similarMatch.get().getCacheKey());
                if (matchEntry != null) {
                    totalSavings.addAndGet((long) (matchEntry.response.getUsage().getEstimatedCost() * 100));
                }
                return similarMatch;
            }
        }

        misses.incrementAndGet();
        return Optional.empty();
    }

    @Override
    public void put(CompletionRequest request, CompletionResponse response) {
        String key = createKey(request);

        CachedEntry entry = new CachedEntry(response, System.currentTimeMillis());
        cache.put(key, entry);

        // Store embedding for semantic similarity
        if (useSemanticSimilarity && embeddingClient != null) {
            try {
                String content = extractContent(request);
                EmbeddingResponse embeddingResponse = embeddingClient.embed(
                        EmbeddingRequest.builder()
                                .model(embeddingModel)
                                .input(List.of(content))
                                .build()
                );

                if (!embeddingResponse.getData().isEmpty()) {
                    embeddings.put(key, embeddingResponse.getData().get(0).getEmbedding());
                }
            } catch (Exception e) {
                // Log error but don't fail the cache operation
                System.err.println("Failed to generate embedding: " + e.getMessage());
            }
        }
    }

    @Override
    public void clear() {
        cache.invalidateAll();
        embeddings.clear();
        hits.set(0);
        misses.set(0);
        totalSavings.set(0);
    }

    @Override
    public CacheStats getStats() {
        long totalRequests = hits.get() + misses.get();
        double hitRate = totalRequests > 0 ? (double) hits.get() / totalRequests : 0.0;

        return CacheStats.builder()
                .hits(hits.get())
                .misses(misses.get())
                .totalRequests(totalRequests)
                .hitRate(hitRate)
                .estimatedSavings(totalSavings.get() / 100.0)
                .currency("USD")
                .build();
    }

    private Optional<CachedResponse> findSimilarMatch(CompletionRequest request, String requestKey) {
        try {
            String content = extractContent(request);
            EmbeddingResponse embeddingResponse = embeddingClient.embed(
                    EmbeddingRequest.builder()
                            .model(embeddingModel)
                            .input(List.of(content))
                            .build()
            );

            if (embeddingResponse.getData().isEmpty()) {
                return Optional.empty();
            }

            List<Double> requestEmbedding = embeddingResponse.getData().get(0).getEmbedding();

            // Find most similar cached entry
            String bestMatchKey = null;
            double bestSimilarity = 0.0;

            for (Map.Entry<String, List<Double>> entry : embeddings.entrySet()) {
                double similarity = cosineSimilarity(requestEmbedding, entry.getValue());
                if (similarity > bestSimilarity && similarity >= similarityThreshold) {
                    bestSimilarity = similarity;
                    bestMatchKey = entry.getKey();
                }
            }

            if (bestMatchKey != null) {
                CachedEntry entry = cache.getIfPresent(bestMatchKey);
                if (entry != null) {
                    return Optional.of(CachedResponse.builder()
                            .response(entry.response)
                            .similarityScore(bestSimilarity)
                            .cachedAt(entry.timestamp)
                            .cacheKey(bestMatchKey)
                            .build());
                }
            }
        } catch (Exception e) {
            System.err.println("Semantic similarity search failed: " + e.getMessage());
        }

        return Optional.empty();
    }

    private String createKey(CompletionRequest request) {
        // Create a deterministic key from request
        StringBuilder sb = new StringBuilder();
        sb.append(request.getModel()).append("|");
        sb.append(request.getTemperature()).append("|");
        sb.append(request.getMaxTokens()).append("|");
        sb.append(extractContent(request));

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(sb.toString().getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return sb.toString();
        }
    }

    private String extractContent(CompletionRequest request) {
        StringBuilder content = new StringBuilder();
        if (request.getMessages() != null) {
            for (var msg : request.getMessages()) {
                content.append(msg.getRole().name()).append(": ");
                content.append(msg.getContent()).append("\n");
            }
        }
        return content.toString();
    }

    private double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private static class CachedEntry {
        final CompletionResponse response;
        final long timestamp;

        CachedEntry(CompletionResponse response, long timestamp) {
            this.response = response;
            this.timestamp = timestamp;
        }
    }
}
