package com.worldclass.ai.core;

/**
 * Supported LLM providers in the Universal Client
 * Feature #1: Universal LLM Client - 12 providers in one API
 */
public enum LLMProvider {
    OPENAI("OpenAI", "https://api.openai.com/v1"),
    ANTHROPIC("Anthropic", "https://api.anthropic.com/v1"),
    GOOGLE("Google AI", "https://generativelanguage.googleapis.com/v1"),
    COHERE("Cohere", "https://api.cohere.ai/v1"),
    MISTRAL("Mistral AI", "https://api.mistral.ai/v1"),
    HUGGINGFACE("HuggingFace", "https://api-inference.huggingface.co/models"),
    AZURE_OPENAI("Azure OpenAI", "https://{resource}.openai.azure.com"),
    AWS_BEDROCK("AWS Bedrock", "bedrock-runtime.{region}.amazonaws.com"),
    VERTEX_AI("Google Vertex AI", "https://{region}-aiplatform.googleapis.com"),
    TOGETHER_AI("Together AI", "https://api.together.xyz/v1"),
    REPLICATE("Replicate", "https://api.replicate.com/v1"),
    OLLAMA("Ollama", "http://localhost:11434/api");

    private final String displayName;
    private final String baseUrl;

    LLMProvider(String displayName, String baseUrl) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
