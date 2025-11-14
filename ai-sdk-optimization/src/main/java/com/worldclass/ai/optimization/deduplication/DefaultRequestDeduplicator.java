package com.worldclass.ai.optimization.deduplication;

import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.model.CompletionRequest;
import com.worldclass.ai.core.model.CompletionResponse;

import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of Request Deduplicator
 */
public class DefaultRequestDeduplicator implements RequestDeduplicator {

    private final LLMClient client;
    private final Map<String, List<CompletableFuture<CompletionResponse>>> pendingRequests;
    private final ScheduledExecutorService scheduler;
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong uniqueRequests = new AtomicLong(0);

    public DefaultRequestDeduplicator(LLMClient client) {
        this.client = client;
        this.pendingRequests = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Flush every 100ms to batch requests
        scheduler.scheduleAtFixedRate(this::flush, 100, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<CompletionResponse> submit(CompletionRequest request) {
        totalRequests.incrementAndGet();

        String requestKey = createKey(request);
        CompletableFuture<CompletionResponse> future = new CompletableFuture<>();

        synchronized (pendingRequests) {
            List<CompletableFuture<CompletionResponse>> futures =
                pendingRequests.computeIfAbsent(requestKey, k -> new ArrayList<>());

            if (futures.isEmpty()) {
                uniqueRequests.incrementAndGet();
            }

            futures.add(future);
        }

        return future;
    }

    @Override
    public void flush() {
        Map<String, List<CompletableFuture<CompletionResponse>>> toProcess;

        synchronized (pendingRequests) {
            if (pendingRequests.isEmpty()) {
                return;
            }
            toProcess = new HashMap<>(pendingRequests);
            pendingRequests.clear();
        }

        // Process each unique request
        for (Map.Entry<String, List<CompletableFuture<CompletionResponse>>> entry : toProcess.entrySet()) {
            String key = entry.getKey();
            List<CompletableFuture<CompletionResponse>> futures = entry.getValue();

            // Make single API call
            CompletableFuture.runAsync(() -> {
                try {
                    CompletionRequest request = reconstructRequest(key);
                    CompletionResponse response = client.complete(request);

                    // Complete all waiting futures
                    for (CompletableFuture<CompletionResponse> future : futures) {
                        future.complete(response);
                    }
                } catch (Exception e) {
                    for (CompletableFuture<CompletionResponse> future : futures) {
                        future.completeExceptionally(e);
                    }
                }
            });
        }
    }

    @Override
    public DeduplicationStats getStats() {
        long total = totalRequests.get();
        long unique = uniqueRequests.get();
        long deduplicated = total - unique;
        double rate = total > 0 ? (double) deduplicated / total : 0.0;

        // Estimate cost savings (assuming $0.01 per request average)
        double savings = deduplicated * 0.01;

        return DeduplicationStats.builder()
            .totalRequests(total)
            .uniqueRequests(unique)
            .deduplicatedRequests(deduplicated)
            .deduplicationRate(rate)
            .costSavings(savings)
            .build();
    }

    private String createKey(CompletionRequest request) {
        // Create deterministic key from request
        StringBuilder sb = new StringBuilder();
        sb.append(request.getModel()).append("|");
        sb.append(request.getTemperature()).append("|");
        request.getMessages().forEach(msg ->
            sb.append(msg.getRole()).append(":").append(msg.getContent()).append("|")
        );

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(sb.toString().getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return sb.toString();
        }
    }

    private CompletionRequest reconstructRequest(String key) {
        // In production, would store original request
        // For now, return null as we have the key
        return null;
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
