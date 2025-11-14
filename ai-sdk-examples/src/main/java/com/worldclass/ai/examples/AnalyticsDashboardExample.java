package com.worldclass.ai.examples;

import com.worldclass.ai.enterprise.analytics.AnalyticsDashboard;
import com.worldclass.ai.enterprise.analytics.InMemoryAnalyticsDashboard;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Feature #34: Analytics Dashboard - Know where your money goes!
 */
public class AnalyticsDashboardExample {

    public static void main(String[] args) {
        AnalyticsDashboard dashboard = new InMemoryAnalyticsDashboard();

        // Simulate API calls
        for (int i = 0; i < 100; i++) {
            dashboard.recordApiCall(
                    AnalyticsDashboard.ApiCallMetrics.builder()
                            .userId("user-" + (i % 10))
                            .model("gpt-4")
                            .provider("OpenAI")
                            .promptTokens(100)
                            .completionTokens(200)
                            .cost(0.009) // $0.009 per request
                            .latencyMs(1000 + (i % 500))
                            .success(i % 20 != 0) // 5% error rate
                            .timestamp(Instant.now().minus(i, ChronoUnit.MINUTES))
                            .build()
            );
        }

        // Get analytics
        Instant end = Instant.now();
        Instant start = end.minus(7, ChronoUnit.DAYS);

        // Cost breakdown
        AnalyticsDashboard.CostBreakdown costs = dashboard.getCostBreakdown(start, end);
        System.out.println("=== Cost Breakdown ===");
        System.out.println("Total Cost: $" + String.format("%.2f", costs.getTotalCost()));
        System.out.println("\nTop Users:");
        costs.getByUser().entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .forEach(e -> System.out.println("  " + e.getKey() + ": $" + String.format("%.2f", e.getValue())));

        // Performance metrics
        AnalyticsDashboard.PerformanceMetrics perf = dashboard.getPerformanceMetrics(start, end);
        System.out.println("\n=== Performance ===");
        System.out.println("Average Latency: " + String.format("%.0f ms", perf.getAverageLatency()));
        System.out.println("P95 Latency: " + String.format("%.0f ms", perf.getP95Latency()));
        System.out.println("Error Rate: " + String.format("%.1f%%", perf.getErrorRate() * 100));

        // Usage statistics
        AnalyticsDashboard.UsageStatistics usage = dashboard.getUsageStatistics(start, end);
        System.out.println("\n=== Usage ===");
        System.out.println("Total Requests: " + usage.getTotalRequests());
        System.out.println("Total Tokens: " + usage.getTotalTokens());
        System.out.println("\nTop Users by Requests:");
        usage.getTopUsers().stream()
                .limit(5)
                .forEach(u -> System.out.println("  " + u.getUserId() +
                        ": " + u.getRequests() + " requests ($" +
                        String.format("%.2f", u.getCost()) + ")"));
    }
}
