package com.worldclass.ai.optimization.loadbalancer;

import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.LLMProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default Load Balancer implementation
 */
public class DefaultLoadBalancer implements LoadBalancer {

    private final List<LLMClient> clients;
    private final Strategy strategy;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private final Map<LLMClient, ClientMetrics> metrics = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public DefaultLoadBalancer(List<LLMClient> clients, Strategy strategy) {
        this.clients = new ArrayList<>(clients);
        this.strategy = strategy;

        // Initialize metrics
        for (LLMClient client : clients) {
            metrics.put(client, new ClientMetrics());
        }
    }

    @Override
    public LLMClient getNextClient() {
        if (clients.isEmpty()) {
            throw new IllegalStateException("No clients available");
        }

        // Filter out unhealthy clients
        List<LLMClient> healthyClients = clients.stream()
            .filter(client -> metrics.get(client).healthy)
            .toList();

        if (healthyClients.isEmpty()) {
            // All unhealthy, reset health
            metrics.values().forEach(m -> m.healthy = true);
            healthyClients = new ArrayList<>(clients);
        }

        LLMClient selected = switch (strategy) {
            case ROUND_ROBIN -> roundRobin(healthyClients);
            case WEIGHTED -> weighted(healthyClients);
            case LATENCY_BASED -> latencyBased(healthyClients);
            case COST_BASED -> costBased(healthyClients);
            case RANDOM -> healthyClients.get(random.nextInt(healthyClients.size()));
        };

        metrics.get(selected).requestCount.incrementAndGet();
        return selected;
    }

    @Override
    public void reportHealth(LLMClient client, boolean healthy) {
        ClientMetrics m = metrics.get(client);
        if (m != null) {
            m.healthy = healthy;
            if (!healthy) {
                m.errorCount.incrementAndGet();
            }
        }
    }

    @Override
    public LoadBalancerStats getStats() {
        List<ClientStats> stats = new ArrayList<>();
        long totalRequests = 0;
        double totalLatency = 0;

        for (Map.Entry<LLMClient, ClientMetrics> entry : metrics.entrySet()) {
            LLMClient client = entry.getKey();
            ClientMetrics m = entry.getValue();

            long requests = m.requestCount.get();
            long errors = m.errorCount.get();
            double avgLatency = m.totalLatency.get() / Math.max(requests, 1);
            double errorRate = requests > 0 ? (double) errors / requests : 0.0;

            stats.add(ClientStats.builder()
                .provider(client.getConfig().getProvider())
                .requestCount(requests)
                .averageLatency(avgLatency)
                .errorRate(errorRate)
                .healthy(m.healthy)
                .build());

            totalRequests += requests;
            totalLatency += m.totalLatency.get();
        }

        return LoadBalancerStats.builder()
            .clientStats(stats)
            .totalRequests(totalRequests)
            .averageLatency(totalRequests > 0 ? totalLatency / totalRequests : 0)
            .build();
    }

    private LLMClient roundRobin(List<LLMClient> healthyClients) {
        int index = roundRobinIndex.getAndIncrement() % healthyClients.size();
        return healthyClients.get(index);
    }

    private LLMClient weighted(List<LLMClient> healthyClients) {
        // Weight by inverse error rate
        double totalWeight = 0;
        Map<LLMClient, Double> weights = new HashMap<>();

        for (LLMClient client : healthyClients) {
            ClientMetrics m = metrics.get(client);
            double errorRate = m.requestCount.get() > 0
                ? (double) m.errorCount.get() / m.requestCount.get()
                : 0.0;
            double weight = 1.0 - errorRate;
            weights.put(client, weight);
            totalWeight += weight;
        }

        double r = random.nextDouble() * totalWeight;
        double cumulative = 0;

        for (Map.Entry<LLMClient, Double> entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (r <= cumulative) {
                return entry.getKey();
            }
        }

        return healthyClients.get(0);
    }

    private LLMClient latencyBased(List<LLMClient> healthyClients) {
        return healthyClients.stream()
            .min((c1, c2) -> {
                ClientMetrics m1 = metrics.get(c1);
                ClientMetrics m2 = metrics.get(c2);
                double avg1 = m1.totalLatency.get() / Math.max(m1.requestCount.get(), 1);
                double avg2 = m2.totalLatency.get() / Math.max(m2.requestCount.get(), 1);
                return Double.compare(avg1, avg2);
            })
            .orElse(healthyClients.get(0));
    }

    private LLMClient costBased(List<LLMClient> healthyClients) {
        // Route to cheapest provider (simplified)
        Map<LLMProvider, Double> costs = Map.of(
            LLMProvider.ANTHROPIC, 3.0,
            LLMProvider.OPENAI, 10.0,
            LLMProvider.GOOGLE, 0.5,
            LLMProvider.MISTRAL, 2.7
        );

        return healthyClients.stream()
            .min((c1, c2) -> {
                double cost1 = costs.getOrDefault(c1.getConfig().getProvider(), 5.0);
                double cost2 = costs.getOrDefault(c2.getConfig().getProvider(), 5.0);
                return Double.compare(cost1, cost2);
            })
            .orElse(healthyClients.get(0));
    }

    private static class ClientMetrics {
        final AtomicLong requestCount = new AtomicLong(0);
        final AtomicLong errorCount = new AtomicLong(0);
        final AtomicLong totalLatency = new AtomicLong(0);
        volatile boolean healthy = true;
    }
}
