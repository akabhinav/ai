package com.worldclass.ai.observability.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Feature #50: Custom Metrics - Build your own dashboards
 *
 * Prometheus metrics for:
 * - Request count
 * - Token usage
 * - Cost tracking
 * - Latency percentiles
 * - Error rates
 * - Cache hit rates
 *
 * Use with Grafana for beautiful dashboards!
 */
public class MetricsCollector {

    private final MeterRegistry registry;

    // Counters
    private final Counter requestCounter;
    private final Counter tokenCounter;
    private final Counter errorCounter;

    // Gauges
    private final Map<String, AtomicDouble> gauges = new HashMap<>();

    // Timers
    private final Timer requestTimer;

    public MetricsCollector(MeterRegistry registry) {
        this.registry = registry;

        // Initialize metrics
        this.requestCounter = Counter.builder("llm.requests.total")
            .description("Total number of LLM requests")
            .tag("sdk", "worldclass-ai")
            .register(registry);

        this.tokenCounter = Counter.builder("llm.tokens.total")
            .description("Total number of tokens used")
            .tag("sdk", "worldclass-ai")
            .register(registry);

        this.errorCounter = Counter.builder("llm.errors.total")
            .description("Total number of errors")
            .tag("sdk", "worldclass-ai")
            .register(registry);

        this.requestTimer = Timer.builder("llm.request.duration")
            .description("LLM request duration")
            .tag("sdk", "worldclass-ai")
            .register(registry);

        // Cost gauge
        AtomicDouble costGauge = new AtomicDouble(0);
        Gauge.builder("llm.cost.total", costGauge, AtomicDouble::get)
            .description("Total LLM cost in USD")
            .tag("sdk", "worldclass-ai")
            .register(registry);
        gauges.put("cost", costGauge);

        // Cache hit rate gauge
        AtomicDouble cacheHitRate = new AtomicDouble(0);
        Gauge.builder("llm.cache.hit_rate", cacheHitRate, AtomicDouble::get)
            .description("Cache hit rate")
            .tag("sdk", "worldclass-ai")
            .register(registry);
        gauges.put("cache_hit_rate", cacheHitRate);
    }

    public void recordRequest(String provider, String model, boolean success) {
        requestCounter.increment();
        Tags tags = Tags.of("provider", provider, "model", model, "success", String.valueOf(success));
        registry.counter("llm.requests", tags).increment();

        if (!success) {
            errorCounter.increment();
        }
    }

    public void recordTokens(String model, int promptTokens, int completionTokens) {
        tokenCounter.increment(promptTokens + completionTokens);

        registry.counter("llm.tokens.prompt", "model", model).increment(promptTokens);
        registry.counter("llm.tokens.completion", "model", model).increment(completionTokens);
    }

    public void recordCost(double cost) {
        gauges.get("cost").addAndGet(cost);
    }

    public void recordLatency(String provider, String model, long latencyMs) {
        requestTimer.record(latencyMs, TimeUnit.MILLISECONDS);

        Timer.builder("llm.latency")
            .tag("provider", provider)
            .tag("model", model)
            .register(registry)
            .record(latencyMs, TimeUnit.MILLISECONDS);
    }

    public void recordCacheHitRate(double hitRate) {
        gauges.get("cache_hit_rate").set(hitRate);
    }

    private static class AtomicDouble {
        private double value;

        AtomicDouble(double initial) {
            this.value = initial;
        }

        synchronized double get() {
            return value;
        }

        synchronized void set(double newValue) {
            this.value = newValue;
        }

        synchronized void addAndGet(double delta) {
            this.value += delta;
        }
    }
}
