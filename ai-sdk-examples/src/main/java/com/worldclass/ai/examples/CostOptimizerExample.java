package com.worldclass.ai.examples;

import com.worldclass.ai.core.model.CompletionRequest;
import com.worldclass.ai.core.model.Message;
import com.worldclass.ai.optimization.cost.CostOptimizer;
import com.worldclass.ai.optimization.cost.DefaultCostOptimizer;

import java.util.List;

/**
 * Feature #12: Cost Optimizer - Automatic savings, pays for itself!
 */
public class CostOptimizerExample {

    public static void main(String[] args) {
        CostOptimizer optimizer = new DefaultCostOptimizer();

        // Original request using GPT-4
        CompletionRequest request = CompletionRequest.builder()
                .model("gpt-4")
                .messages(List.of(
                        Message.builder()
                                .role(Message.Role.USER)
                                .content("What is 2+2?") // Simple task
                                .build()
                ))
                .build();

        // Optimize!
        CostOptimizer.OptimizedRequest optimized = optimizer.optimize(request);

        System.out.println("Original Model: " + request.getModel());
        System.out.println("Original Cost: $" + String.format("%.4f", optimized.getOriginalCost()));
        System.out.println("\nOptimized Model: " + optimized.getRecommendedModel());
        System.out.println("Optimized Cost: $" + String.format("%.4f", optimized.getEstimatedCost()));
        System.out.println("\nSavings: " + String.format("%.1f%%", optimized.getSavingsPercent()));
        System.out.println("Reason: " + optimized.getReason());

        // Cost report
        CostOptimizer.CostReport report = optimizer.analyze(request);
        System.out.println("\nMonthly Savings (10K requests): $" + String.format("%.2f", report.getMonthlySavings()));
    }
}
