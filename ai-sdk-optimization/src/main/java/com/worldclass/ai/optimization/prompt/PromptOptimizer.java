package com.worldclass.ai.optimization.prompt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Feature #13: Prompt Optimizer AI - Improves your prompts 40%
 *
 * Uses ML to automatically improve prompts for:
 * - Better quality responses
 * - Reduced token usage
 * - Higher consistency
 * - A/B testing
 */
public interface PromptOptimizer {

    /**
     * Optimize a prompt for better results
     */
    OptimizedPrompt optimize(String prompt);

    /**
     * Optimize with specific goal
     */
    OptimizedPrompt optimize(String prompt, OptimizationGoal goal);

    /**
     * A/B test two prompts
     */
    ABTestResult comparePrompts(String promptA, String promptB, int numTests);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class OptimizedPrompt {
        private String originalPrompt;
        private String optimizedPrompt;
        private double expectedImprovement;
        private List<String> improvements;
        private Map<String, Object> metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ABTestResult {
        private String promptA;
        private String promptB;
        private String winner;
        private double confidenceLevel;
        private Map<String, Double> metrics;
    }

    enum OptimizationGoal {
        QUALITY,        // Maximize response quality
        COST,          // Minimize token usage
        CONSISTENCY,   // Increase consistency
        SPEED          // Reduce latency
    }
}
