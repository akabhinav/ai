package com.worldclass.ai.examples;

import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.LLMProvider;
import com.worldclass.ai.core.model.*;

import java.util.List;

/**
 * Feature #2: Streaming Responses - Real-time token delivery
 */
public class StreamingExample {

    public static void main(String[] args) {
        LLMClient client = LLMClient.builder()
                .provider(LLMProvider.OPENAI)
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();

        System.out.print("Response: ");

        // Stream tokens in real-time
        client.completeStream(
                        CompletionRequest.builder()
                                .model("gpt-4")
                                .messages(List.of(
                                        Message.builder()
                                                .role(Message.Role.USER)
                                                .content("Write a haiku about AI")
                                                .build()
                                ))
                                .stream(true)
                                .build()
                )
                .subscribe(chunk -> {
                    // Print each token as it arrives
                    String content = chunk.getChoices().get(0).getMessage().getContent();
                    if (content != null) {
                        System.out.print(content);
                    }
                });
    }
}
