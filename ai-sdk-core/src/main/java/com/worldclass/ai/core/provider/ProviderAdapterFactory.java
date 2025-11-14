package com.worldclass.ai.core.provider;

import com.worldclass.ai.core.LLMProvider;

/**
 * Factory for creating provider-specific adapters
 */
public class ProviderAdapterFactory {

    public static LLMProviderAdapter create(LLMProvider provider) {
        return switch (provider) {
            case OPENAI, AZURE_OPENAI -> new OpenAIAdapter();
            case ANTHROPIC -> new AnthropicAdapter();
            case GOOGLE -> new GoogleAIAdapter();
            case COHERE -> new CohereAdapter();
            case MISTRAL -> new MistralAdapter();
            case HUGGINGFACE -> new HuggingFaceAdapter();
            case AWS_BEDROCK -> new BedrockAdapter();
            case VERTEX_AI -> new VertexAIAdapter();
            case TOGETHER_AI -> new TogetherAIAdapter();
            case REPLICATE -> new ReplicateAdapter();
            case OLLAMA -> new OllamaAdapter();
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }
}
