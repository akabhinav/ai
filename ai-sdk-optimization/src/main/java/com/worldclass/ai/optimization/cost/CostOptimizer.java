package com.worldclass.ai.optimization.cost;

import com.worldclass.ai.core.LLMProvider;
import com.worldclass.ai.core.model.CompletionRequest;
import lombok.Builder;
import lombok.Data;

/**
 * Feature #12: Cost Optimizer - AI reduces your bill
 *
 * Automatically optimizes costs by:
 * - Routing to cheapest provider that meets quality requirements
 * - Model downgrading when appropriate (GPT-4 → GPT-3.5 for simple tasks)
 * - Automatic prompt compression
 * - Smart token management
 *
 * Pays for itself! Saves 30-50% on average.
 */
public interface CostOptimizer {

    /**
     * Optimize a request for cost while maintaining quality
     */
    OptimizedRequest optimize(CompletionRequest request);

    /**
     * Get cost optimization recommendations
     */
    CostReport analyze(CompletionRequest request);

    @Data
    @Builder
    class OptimizedRequest {
        private CompletionRequest request;
        private LLMProvider recommendedProvider;
        private String recommendedModel;
        private double estimatedCost;
        private double originalCost;
        private double savingsPercent;
        private String reason;
    }

    @Data
    @Builder
    class CostReport {
        private double currentCost;
        private double optimizedCost;
        private double monthlySavings;
        private String recommendation;
    }
}
