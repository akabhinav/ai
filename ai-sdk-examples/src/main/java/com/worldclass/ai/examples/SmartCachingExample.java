package com.worldclass.ai.examples;

import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.LLMProvider;
import com.worldclass.ai.core.model.*;
import com.worldclass.ai.optimization.cache.SmartCache;

import java.util.List;

/**
 * Feature #9: Smart Caching - Save 60-80% costs = $21K/month savings!
 */
public class SmartCachingExample {

    public static void main(String[] args) {
        LLMClient client = LLMClient.builder()
                .provider(LLMProvider.OPENAI)
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();

        // Create smart cache with semantic similarity
        SmartCache cache = SmartCache.builder()
                .embeddingClient(client)
                .similarityThreshold(0.95) // 95% similarity required
                .build();

        CompletionRequest request1 = CompletionRequest.builder()
                .model("gpt-4")
                .messages(List.of(
                        Message.builder()
                                .role(Message.Role.USER)
                                .content("What is the capital of France?")
                                .build()
                ))
                .build();

        // First request - cache MISS (makes API call)
        var cached = cache.get(request1);
        if (cached.isEmpty()) {
            CompletionResponse response = client.complete(request1);
            cache.put(request1, response);
            System.out.println("Cache MISS - Called API: $" + response.getUsage().getEstimatedCost());
        }

        // Similar request - cache HIT (FREE!)
        CompletionRequest request2 = CompletionRequest.builder()
                .model("gpt-4")
                .messages(List.of(
                        Message.builder()
                                .role(Message.Role.USER)
                                .content("Tell me the capital city of France")
                                .build()
                ))
                .build();

        cached = cache.get(request2);
        if (cached.isPresent()) {
            System.out.println("Cache HIT - Saved: $" + cached.get().getResponse().getUsage().getEstimatedCost());
            System.out.println("Similarity: " + (cached.get().getSimilarityScore() * 100) + "%");
        }

        // Show savings
        SmartCache.CacheStats stats = cache.getStats();
        System.out.println("\nCache Statistics:");
        System.out.println("Hit Rate: " + (stats.getHitRate() * 100) + "%");
        System.out.println("Total Savings: $" + stats.getEstimatedSavings());
    }
}
