package com.worldclass.ai.enterprise.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Feature #34: Analytics Dashboard - Deep insights
 *
 * Track and analyze:
 * - Cost per user, model, feature
 * - Performance metrics (latency, throughput)
 * - Usage patterns
 * - Error rates
 * - Custom reports
 *
 * Know where your money goes!
 */
public interface AnalyticsDashboard {

    /**
     * Record API call
     */
    void recordApiCall(ApiCallMetrics metrics);

    /**
     * Get cost breakdown
     */
    CostBreakdown getCostBreakdown(Instant start, Instant end);

    /**
     * Get performance metrics
     */
    PerformanceMetrics getPerformanceMetrics(Instant start, Instant end);

    /**
     * Get usage statistics
     */
    UsageStatistics getUsageStatistics(Instant start, Instant end);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ApiCallMetrics {
        private String userId;
        private String model;
        private String provider;
        private int promptTokens;
        private int completionTokens;
        private double cost;
        private long latencyMs;
        private boolean success;
        private Instant timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CostBreakdown {
        private double totalCost;
        private Map<String, Double> byUser;
        private Map<String, Double> byModel;
        private Map<String, Double> byProvider;
        private List<DailyCost> dailyCosts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class DailyCost {
        private String date;
        private double cost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PerformanceMetrics {
        private double averageLatency;
        private double p95Latency;
        private double p99Latency;
        private long totalRequests;
        private long successfulRequests;
        private double errorRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UsageStatistics {
        private long totalRequests;
        private long totalTokens;
        private Map<String, Long> requestsByModel;
        private Map<String, Long> requestsByUser;
        private List<TopUser> topUsers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class TopUser {
        private String userId;
        private long requests;
        private double cost;
    }
}
