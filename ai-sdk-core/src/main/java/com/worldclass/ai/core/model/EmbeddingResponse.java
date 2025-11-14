package com.worldclass.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Feature #5: Embeddings & Vectors response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {
    private List<EmbeddingData> data;
    private String model;
    private CompletionResponse.Usage usage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddingData {
        private Integer index;
        private List<Double> embedding;
        private String object;
    }
}
