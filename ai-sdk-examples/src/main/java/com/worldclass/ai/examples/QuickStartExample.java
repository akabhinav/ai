package com.worldclass.ai.examples;

import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.LLMProvider;
import com.worldclass.ai.core.model.*;

import java.util.List;

/**
 * Quick Start - Get running in 30 seconds!
 */
public class QuickStartExample {

    public static void main(String[] args) {
        // Feature #1: Universal LLM Client - Switch providers with one line
        LLMClient client = LLMClient.builder()
                .provider(LLMProvider.OPENAI)
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();

        // Simple completion
        CompletionResponse response = client.complete(
                CompletionRequest.builder()
                        .model("gpt-4")
                        .messages(List.of(
                                Message.builder()
                                        .role(Message.Role.USER)
                                        .content("What is the capital of France?")
                                        .build()
                        ))
                        .build()
        );

        System.out.println("Answer: " + response.getChoices().get(0).getMessage().getContent());

        // Feature #6: Token Management - Automatic cost tracking
        System.out.println("Tokens used: " + response.getUsage().getTotalTokens());
        System.out.println("Cost: $" + response.getUsage().getEstimatedCost());

        // Switch to Anthropic with ONE LINE
        client = client.withProvider(LLMProvider.ANTHROPIC);
    }
}
