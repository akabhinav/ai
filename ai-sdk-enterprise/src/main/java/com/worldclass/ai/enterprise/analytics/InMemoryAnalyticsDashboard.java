package com.worldclass.ai.enterprise.analytics;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * In-memory analytics dashboard
 */
public class InMemoryAnalyticsDashboard implements AnalyticsDashboard {

    private final Queue<ApiCallMetrics> metrics = new ConcurrentLinkedQueue<>();

    @Override
    public void recordApiCall(ApiCallMetrics call) {
        if (call.getTimestamp() == null) {
            call.setTimestamp(Instant.now());
        }
        metrics.add(call);
    }

    @Override
    public CostBreakdown getCostBreakdown(Instant start, Instant end) {
        List<ApiCallMetrics> filtered = filterByTime(start, end);

        double total = filtered.stream().mapToDouble(ApiCallMetrics::getCost).sum();

        Map<String, Double> byUser = filtered.stream()
            .collect(Collectors.groupingBy(
                ApiCallMetrics::getUserId,
                Collectors.summingDouble(ApiCallMetrics::getCost)
            ));

        Map<String, Double> byModel = filtered.stream()
            .collect(Collectors.groupingBy(
                ApiCallMetrics::getModel,
                Collectors.summingDouble(ApiCallMetrics::getCost)
            ));

        Map<String, Double> byProvider = filtered.stream()
            .collect(Collectors.groupingBy(
                ApiCallMetrics::getProvider,
                Collectors.summingDouble(ApiCallMetrics::getCost)
            ));

        Map<String, Double> dailyCostMap = filtered.stream()
            .collect(Collectors.groupingBy(
                m -> LocalDate.ofInstant(m.getTimestamp(), ZoneId.systemDefault()).toString(),
                Collectors.summingDouble(ApiCallMetrics::getCost)
            ));

        List<DailyCost> dailyCosts = dailyCostMap.entrySet().stream()
            .map(e -> DailyCost.builder().date(e.getKey()).cost(e.getValue()).build())
            .sorted(Comparator.comparing(DailyCost::getDate))
            .collect(Collectors.toList());

        return CostBreakdown.builder()
            .totalCost(total)
            .byUser(byUser)
            .byModel(byModel)
            .byProvider(byProvider)
            .dailyCosts(dailyCosts)
            .build();
    }

    @Override
    public PerformanceMetrics getPerformanceMetrics(Instant start, Instant end) {
        List<ApiCallMetrics> filtered = filterByTime(start, end);

        long total = filtered.size();
        long successful = filtered.stream().filter(ApiCallMetrics::isSuccess).count();

        List<Long> latencies = filtered.stream()
            .map(ApiCallMetrics::getLatencyMs)
            .sorted()
            .collect(Collectors.toList());

        double avgLatency = latencies.isEmpty() ? 0 :
            latencies.stream().mapToLong(Long::longValue).average().orElse(0);

        double p95 = latencies.isEmpty() ? 0 :
            latencies.get((int) (latencies.size() * 0.95));

        double p99 = latencies.isEmpty() ? 0 :
            latencies.get((int) (latencies.size() * 0.99));

        return PerformanceMetrics.builder()
            .averageLatency(avgLatency)
            .p95Latency(p95)
            .p99Latency(p99)
            .totalRequests(total)
            .successfulRequests(successful)
            .errorRate(total > 0 ? 1.0 - ((double) successful / total) : 0)
            .build();
    }

    @Override
    public UsageStatistics getUsageStatistics(Instant start, Instant end) {
        List<ApiCallMetrics> filtered = filterByTime(start, end);

        long totalRequests = filtered.size();
        long totalTokens = filtered.stream()
            .mapToLong(m -> m.getPromptTokens() + m.getCompletionTokens())
            .sum();

        Map<String, Long> byModel = filtered.stream()
            .collect(Collectors.groupingBy(
                ApiCallMetrics::getModel,
                Collectors.counting()
            ));

        Map<String, Long> byUser = filtered.stream()
            .collect(Collectors.groupingBy(
                ApiCallMetrics::getUserId,
                Collectors.counting()
            ));

        Map<String, Double> userCosts = filtered.stream()
            .collect(Collectors.groupingBy(
                ApiCallMetrics::getUserId,
                Collectors.summingDouble(ApiCallMetrics::getCost)
            ));

        List<TopUser> topUsers = byUser.entrySet().stream()
            .map(e -> TopUser.builder()
                .userId(e.getKey())
                .requests(e.getValue())
                .cost(userCosts.getOrDefault(e.getKey(), 0.0))
                .build())
            .sorted((a, b) -> Long.compare(b.getRequests(), a.getRequests()))
            .limit(10)
            .collect(Collectors.toList());

        return UsageStatistics.builder()
            .totalRequests(totalRequests)
            .totalTokens(totalTokens)
            .requestsByModel(byModel)
            .requestsByUser(byUser)
            .topUsers(topUsers)
            .build();
    }

    private List<ApiCallMetrics> filterByTime(Instant start, Instant end) {
        return metrics.stream()
            .filter(m -> m.getTimestamp().isAfter(start) && m.getTimestamp().isBefore(end))
            .collect(Collectors.toList());
    }
}
