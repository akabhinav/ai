package com.worldclass.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Unified completion response from all providers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionResponse {
    private String id;
    private String model;
    private Instant created;
    private List<Choice> choices;
    private Usage usage;
    private Map<String, Object> metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private Integer index;
        private Message message;
        private String finishReason;
    }

    /**
     * Feature #6: Token Management - Precise cost tracking
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;

        // Cost tracking
        private Double estimatedCost;
        private String currency;

        // Detailed breakdown
        private Map<String, Integer> tokenBreakdown;
    }
}
