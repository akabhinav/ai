package com.worldclass.ai.optimization.loadbalancer;

import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.LLMProvider;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Feature #16: Load Balancing - Multi-provider distribution
 *
 * Distributes requests across multiple providers/regions for:
 * - High availability
 * - Better performance
 * - Cost optimization
 * - Rate limit avoidance
 *
 * Strategies:
 * - Round-robin
 * - Weighted (by cost, performance, etc.)
 * - Latency-based (route to fastest)
 * - Cost-based (route to cheapest)
 */
public interface LoadBalancer {

    /**
     * Get next client based on load balancing strategy
     */
    LLMClient getNextClient();

    /**
     * Report health status of a client
     */
    void reportHealth(LLMClient client, boolean healthy);

    /**
     * Get load balancing statistics
     */
    LoadBalancerStats getStats();

    @Data
    @Builder
    class LoadBalancerStats {
        private List<ClientStats> clientStats;
        private long totalRequests;
        private double averageLatency;
    }

    @Data
    @Builder
    class ClientStats {
        private LLMProvider provider;
        private long requestCount;
        private double averageLatency;
        private double errorRate;
        private boolean healthy;
    }

    enum Strategy {
        ROUND_ROBIN,
        WEIGHTED,
        LATENCY_BASED,
        COST_BASED,
        RANDOM
    }
}
