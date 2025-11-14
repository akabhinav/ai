package com.worldclass.ai.core.model;

import com.worldclass.ai.core.LLMProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Feature #5: Embeddings & Vectors - Semantic search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRequest {
    private LLMProvider provider;
    private String model;
    private List<String> input;
    private String user;

    @Builder.Default
    private Integer dimensions = null; // For providers that support dimension reduction
}
