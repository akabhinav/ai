package com.worldclass.ai.observability.telemetry;

import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.model.CompletionRequest;
import com.worldclass.ai.core.model.CompletionResponse;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

/**
 * Feature #49: OpenTelemetry - Automatic distributed tracing
 *
 * Automatically traces:
 * - Every LLM API call
 * - Token usage
 * - Latency
 * - Errors
 * - Custom attributes
 *
 * Integrates with: Datadog, New Relic, Jaeger, Zipkin, etc.
 */
public class OpenTelemetryIntegration {

    private final Tracer tracer;

    public OpenTelemetryIntegration(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("worldclass-ai-sdk");
    }

    /**
     * Wrap LLM client with automatic tracing
     */
    public CompletionResponse traceCompletion(
            LLMClient client,
            CompletionRequest request,
            String operationName
    ) {
        Span span = tracer.spanBuilder(operationName).startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Add attributes
            span.setAttribute("llm.provider", request.getProvider().name());
            span.setAttribute("llm.model", request.getModel());
            span.setAttribute("llm.temperature", request.getTemperature());

            // Execute request
            long startTime = System.currentTimeMillis();
            CompletionResponse response = client.complete(request);
            long duration = System.currentTimeMillis() - startTime;

            // Add response attributes
            if (response.getUsage() != null) {
                span.setAttribute("llm.tokens.prompt", response.getUsage().getPromptTokens());
                span.setAttribute("llm.tokens.completion", response.getUsage().getCompletionTokens());
                span.setAttribute("llm.tokens.total", response.getUsage().getTotalTokens());
                span.setAttribute("llm.cost", response.getUsage().getEstimatedCost());
            }
            span.setAttribute("llm.latency_ms", duration);

            return response;

        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
