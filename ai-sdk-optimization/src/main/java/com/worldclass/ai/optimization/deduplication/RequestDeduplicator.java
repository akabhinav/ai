package com.worldclass.ai.optimization.deduplication;

import com.worldclass.ai.core.model.CompletionRequest;
import com.worldclass.ai.core.model.CompletionResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Feature #15: Request Deduplication - Batch identical requests
 *
 * Automatically detects and batches identical or similar requests
 * to make a single API call instead of multiple.
 *
 * Savings: If you have 100 identical requests, make 1 API call instead of 100!
 */
public interface RequestDeduplicator {

    /**
     * Submit a request for deduplication
     * Returns a future that completes when the response is available
     */
    CompletableFuture<CompletionResponse> submit(CompletionRequest request);

    /**
     * Get deduplication statistics
     */
    DeduplicationStats getStats();

    /**
     * Clear pending requests
     */
    void flush();

    @Data
    @Builder
    class DeduplicationStats {
        private long totalRequests;
        private long uniqueRequests;
        private long deduplicatedRequests;
        private double deduplicationRate;
        private double costSavings;
    }
}
