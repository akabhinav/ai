package com.worldclass.ai.core.model;

import com.worldclass.ai.core.LLMProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Unified completion request for all providers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionRequest {
    private LLMProvider provider;
    private String model;
    private List<Message> messages;

    @Builder.Default
    private Double temperature = 0.7;

    @Builder.Default
    private Integer maxTokens = 1000;

    private Double topP;
    private Integer topK;
    private List<String> stop;
    private Boolean stream;

    // Feature #4: Function Calling
    private List<FunctionDefinition> functions;
    private String functionCall; // "auto", "none", or specific function name

    // Feature #8: Structured Output
    private Map<String, Object> responseFormat;

    // Feature #6: Token Management
    @Builder.Default
    private Boolean trackTokens = true;

    private String user;
    private Map<String, Object> metadata;
}
