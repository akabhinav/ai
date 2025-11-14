package com.worldclass.ai.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Feature #4: Function Calling - LLMs call your Java methods
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionDefinition {
    private String name;
    private String description;
    private Map<String, Object> parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {
        private String type;
        private String description;
        private Boolean required;
        private Object defaultValue;
        private JsonNode enumValues;
    }
}
