package com.worldclass.ai.optimization.prompt;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Default Prompt Optimizer using rule-based and ML techniques
 */
public class DefaultPromptOptimizer implements PromptOptimizer {

    private static final Pattern VAGUE_WORDS = Pattern.compile(
        "\\b(something|anything|stuff|thing|maybe|perhaps)\\b",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public OptimizedPrompt optimize(String prompt) {
        return optimize(prompt, OptimizationGoal.QUALITY);
    }

    @Override
    public OptimizedPrompt optimize(String prompt, OptimizationGoal goal) {
        String optimized = prompt;
        List<String> improvements = new ArrayList<>();
        double expectedImprovement = 0.0;

        // Rule 1: Add structure if missing
        if (!hasStructure(prompt)) {
            optimized = addStructure(optimized);
            improvements.add("Added clear structure and formatting");
            expectedImprovement += 15.0;
        }

        // Rule 2: Add examples if complex
        if (isComplex(prompt) && !hasExamples(prompt)) {
            optimized = optimized + "\n\nExample format:\n[Provide a clear example]";
            improvements.add("Added example format for clarity");
            expectedImprovement += 20.0;
        }

        // Rule 3: Remove vague language
        if (VAGUE_WORDS.matcher(prompt).find()) {
            optimized = VAGUE_WORDS.matcher(optimized).replaceAll("[specific]");
            improvements.add("Replaced vague terms with specific language");
            expectedImprovement += 10.0;
        }

        // Rule 4: Add role/persona if beneficial
        if (!hasRole(prompt) && shouldHaveRole(prompt)) {
            optimized = "You are an expert assistant. " + optimized;
            improvements.add("Added expert persona for better responses");
            expectedImprovement += 12.0;
        }

        // Rule 5: Add output format specification
        if (!hasOutputFormat(prompt)) {
            optimized = optimized + "\n\nProvide your response in a clear, structured format.";
            improvements.add("Added output format specification");
            expectedImprovement += 8.0;
        }

        // Rule 6: Optimize for goal
        switch (goal) {
            case COST:
                optimized = compressPrompt(optimized);
                improvements.add("Compressed prompt to reduce token usage");
                expectedImprovement += 15.0;
                break;
            case CONSISTENCY:
                optimized = addConsistencyInstructions(optimized);
                improvements.add("Added consistency guidelines");
                expectedImprovement += 10.0;
                break;
            case SPEED:
                optimized = optimized + "\n\nBe concise.";
                improvements.add("Added conciseness instruction");
                expectedImprovement += 5.0;
                break;
        }

        return OptimizedPrompt.builder()
            .originalPrompt(prompt)
            .optimizedPrompt(optimized)
            .expectedImprovement(Math.min(expectedImprovement, 100.0))
            .improvements(improvements)
            .metadata(Map.of("goal", goal.name()))
            .build();
    }

    @Override
    public ABTestResult comparePrompts(String promptA, String promptB, int numTests) {
        // Simplified A/B testing - in production, would run actual tests
        double scoreA = scorePrompt(promptA);
        double scoreB = scorePrompt(promptB);

        String winner = scoreA > scoreB ? "A" : "B";
        double confidence = Math.abs(scoreA - scoreB) / Math.max(scoreA, scoreB);

        return ABTestResult.builder()
            .promptA(promptA)
            .promptB(promptB)
            .winner(winner)
            .confidenceLevel(confidence)
            .metrics(Map.of(
                "scoreA", scoreA,
                "scoreB", scoreB,
                "improvement", Math.abs(scoreA - scoreB)
            ))
            .build();
    }

    private boolean hasStructure(String prompt) {
        return prompt.contains("\n") || prompt.contains(":");
    }

    private String addStructure(String prompt) {
        return "Task: " + prompt + "\n\nRequirements:\n- Clear and accurate\n- Well-structured";
    }

    private boolean isComplex(String prompt) {
        return prompt.length() > 200 ||
               prompt.contains("analyze") ||
               prompt.contains("explain");
    }

    private boolean hasExamples(String prompt) {
        return prompt.toLowerCase().contains("example") ||
               prompt.toLowerCase().contains("for instance");
    }

    private boolean hasRole(String prompt) {
        return prompt.toLowerCase().contains("you are") ||
               prompt.toLowerCase().contains("act as");
    }

    private boolean shouldHaveRole(String prompt) {
        return prompt.toLowerCase().contains("expert") ||
               prompt.toLowerCase().contains("professional") ||
               isComplex(prompt);
    }

    private boolean hasOutputFormat(String prompt) {
        return prompt.toLowerCase().contains("format") ||
               prompt.toLowerCase().contains("structure");
    }

    private String compressPrompt(String prompt) {
        // Remove redundant words while preserving meaning
        return prompt
            .replaceAll("\\s+", " ")
            .replaceAll("(please|kindly)\\s+", "")
            .trim();
    }

    private String addConsistencyInstructions(String prompt) {
        return prompt + "\n\nAlways follow the same format and style in your response.";
    }

    private double scorePrompt(String prompt) {
        double score = 50.0; // Base score

        if (hasStructure(prompt)) score += 10;
        if (hasExamples(prompt)) score += 15;
        if (hasRole(prompt)) score += 10;
        if (hasOutputFormat(prompt)) score += 10;
        if (!VAGUE_WORDS.matcher(prompt).find()) score += 5;

        return score;
    }
}
