package com.worldclass.ai.optimization.ratelimit;

import com.worldclass.ai.core.model.CompletionRequest;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * Feature #10: Rate Limiting - Never hit API limits
 *
 * Automatically manages rate limits for all providers.
 * Prevents 429 errors and implements smart backoff.
 *
 * Supports:
 * - Token-based rate limiting (e.g., 90K TPM for GPT-4)
 * - Request-based rate limiting (e.g., 3500 RPM for GPT-4)
 * - Per-user/tenant rate limiting
 * - Automatic quota management
 */
public interface RateLimiter {

    /**
     * Acquire permission to make a request
     * Blocks if rate limit would be exceeded
     */
    void acquire(CompletionRequest request);

    /**
     * Try to acquire permission without blocking
     * Returns true if acquired, false if rate limit exceeded
     */
    boolean tryAcquire(CompletionRequest request);

    /**
     * Try to acquire with timeout
     */
    boolean tryAcquire(CompletionRequest request, Duration timeout);

    /**
     * Release resources after request completes
     */
    void release(CompletionRequest request, int tokensUsed);

    /**
     * Get current rate limit status
     */
    RateLimitStatus getStatus();

    @Data
    @Builder
    class RateLimitStatus {
        private long requestsPerMinute;
        private long tokensPerMinute;
        private long currentRequests;
        private long currentTokens;
        private double utilizationPercent;
        private long estimatedWaitTimeMs;
    }
}
