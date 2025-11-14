package com.worldclass.ai.core.prompt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feature #7: Prompt Templates - Version-controlled prompts
 *
 * Allows you to define reusable prompts with variable substitution.
 * Supports versioning, testing, and A/B testing of prompts.
 *
 * Example:
 * <pre>
 * PromptTemplate template = PromptTemplate.builder()
 *     .name("summarize")
 *     .version("1.0")
 *     .template("Summarize the following text in {{style}} style:\n\n{{text}}")
 *     .build();
 *
 * String prompt = template.render(Map.of(
 *     "style", "professional",
 *     "text", "Long text here..."
 * ));
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {
    private String name;
    private String version;
    private String template;
    private String description;

    @Builder.Default
    private Map<String, Object> defaultValues = new HashMap<>();

    private Map<String, VariableDefinition> variables;

    /**
     * Render the template with provided variables
     */
    public String render(Map<String, Object> variables) {
        String result = template;

        // Merge with default values
        Map<String, Object> allVars = new HashMap<>(defaultValues);
        allVars.putAll(variables);

        // Replace {{variable}} with values
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(result);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            Object value = allVars.get(varName);

            if (value == null) {
                throw new IllegalArgumentException("Missing variable: " + varName);
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Validate that all required variables are provided
     */
    public void validate(Map<String, Object> variables) {
        if (this.variables == null) {
            return;
        }

        for (Map.Entry<String, VariableDefinition> entry : this.variables.entrySet()) {
            String varName = entry.getKey();
            VariableDefinition def = entry.getValue();

            if (def.isRequired() && !variables.containsKey(varName) && !defaultValues.containsKey(varName)) {
                throw new IllegalArgumentException("Required variable missing: " + varName);
            }
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariableDefinition {
        private String name;
        private String description;
        private String type;
        private boolean required;
        private Object defaultValue;
    }
}
