package com.worldclass.ai.optimization.ratelimit;

import com.worldclass.ai.core.LLMProvider;
import com.worldclass.ai.core.model.CompletionRequest;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of RateLimiter using token bucket algorithm
 */
public class DefaultRateLimiter implements RateLimiter {
    private final long requestsPerMinute;
    private final long tokensPerMinute;
    private final Semaphore requestSemaphore;
    private final AtomicLong tokenBucket;
    private final long bucketRefillTimeMs = 60000; // 1 minute
    private long lastRefillTimestamp;

    public DefaultRateLimiter(LLMProvider provider, String model) {
        // Provider-specific limits (examples)
        this.requestsPerMinute = getRequestLimit(provider, model);
        this.tokensPerMinute = getTokenLimit(provider, model);
        this.requestSemaphore = new Semaphore((int) requestsPerMinute);
        this.tokenBucket = new AtomicLong(tokensPerMinute);
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    @Override
    public void acquire(CompletionRequest request) {
        try {
            refillBucketIfNeeded();

            // Acquire request permit
            requestSemaphore.acquire();

            // Estimate tokens and acquire from bucket
            int estimatedTokens = estimateTokens(request);
            while (!tryAcquireTokens(estimatedTokens)) {
                Thread.sleep(100);
                refillBucketIfNeeded();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Rate limiter interrupted", e);
        }
    }

    @Override
    public boolean tryAcquire(CompletionRequest request) {
        refillBucketIfNeeded();

        if (!requestSemaphore.tryAcquire()) {
            return false;
        }

        int estimatedTokens = estimateTokens(request);
        if (!tryAcquireTokens(estimatedTokens)) {
            requestSemaphore.release();
            return false;
        }

        return true;
    }

    @Override
    public boolean tryAcquire(CompletionRequest request, Duration timeout) {
        try {
            refillBucketIfNeeded();

            if (!requestSemaphore.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                return false;
            }

            int estimatedTokens = estimateTokens(request);
            long deadline = System.currentTimeMillis() + timeout.toMillis();

            while (!tryAcquireTokens(estimatedTokens)) {
                if (System.currentTimeMillis() >= deadline) {
                    requestSemaphore.release();
                    return false;
                }
                Thread.sleep(100);
                refillBucketIfNeeded();
            }

            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void release(CompletionRequest request, int tokensUsed) {
        requestSemaphore.release();
        // Optionally adjust token estimates based on actual usage
    }

    @Override
    public RateLimitStatus getStatus() {
        refillBucketIfNeeded();

        return RateLimitStatus.builder()
                .requestsPerMinute(requestsPerMinute)
                .tokensPerMinute(tokensPerMinute)
                .currentRequests(requestSemaphore.availablePermits())
                .currentTokens(tokenBucket.get())
                .utilizationPercent(100.0 * (tokensPerMinute - tokenBucket.get()) / tokensPerMinute)
                .build();
    }

    private synchronized void refillBucketIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastRefillTimestamp >= bucketRefillTimeMs) {
            tokenBucket.set(tokensPerMinute);
            lastRefillTimestamp = now;
        }
    }

    private boolean tryAcquireTokens(int tokens) {
        while (true) {
            long current = tokenBucket.get();
            if (current < tokens) {
                return false;
            }
            if (tokenBucket.compareAndSet(current, current - tokens)) {
                return true;
            }
        }
    }

    private int estimateTokens(CompletionRequest request) {
        // Rough estimation - in production use tiktoken
        int promptTokens = 0;
        for (var msg : request.getMessages()) {
            promptTokens += msg.getContent().length() / 4;
        }
        int completionTokens = request.getMaxTokens() != null ? request.getMaxTokens() : 1000;
        return promptTokens + completionTokens;
    }

    private long getRequestLimit(LLMProvider provider, String model) {
        return switch (provider) {
            case OPENAI -> model.contains("gpt-4") ? 500 : 3500;
            case ANTHROPIC -> 50;
            case GOOGLE -> 60;
            default -> 100;
        };
    }

    private long getTokenLimit(LLMProvider provider, String model) {
        return switch (provider) {
            case OPENAI -> model.contains("gpt-4") ? 40000 : 90000;
            case ANTHROPIC -> 100000;
            case GOOGLE -> 60000;
            default -> 50000;
        };
    }
}
