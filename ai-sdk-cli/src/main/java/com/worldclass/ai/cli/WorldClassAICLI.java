package com.worldclass.ai.cli;

import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.LLMProvider;
import com.worldclass.ai.core.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Feature #42: CLI Tool - Command-line interface
 *
 * Usage:
 *   worldclass-ai chat "What is the capital of France?"
 *   worldclass-ai test-prompt prompts/my-prompt.txt
 *   worldclass-ai analyze-costs --last-30-days
 *   worldclass-ai compare-models gpt-4 claude-3-opus
 */
public class WorldClassAICLI {

    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0];

        switch (command) {
            case "chat" -> handleChat(args);
            case "version" -> System.out.println("WorldClass AI SDK v" + VERSION);
            case "help" -> printUsage();
            case "interactive" -> interactiveMode();
            default -> {
                System.err.println("Unknown command: " + command);
                printUsage();
            }
        }
    }

    private static void handleChat(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: worldclass-ai chat <message>");
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String apiKey = System.getenv("OPENAI_API_KEY");

        if (apiKey == null) {
            System.err.println("Error: OPENAI_API_KEY environment variable not set");
            return;
        }

        System.out.println("Sending request to GPT-4...\n");

        try {
            LLMClient client = LLMClient.builder()
                .provider(LLMProvider.OPENAI)
                .apiKey(apiKey)
                .build();

            CompletionResponse response = client.complete(
                CompletionRequest.builder()
                    .model("gpt-4")
                    .messages(List.of(
                        Message.builder()
                            .role(Message.Role.USER)
                            .content(message)
                            .build()
                    ))
                    .build()
            );

            System.out.println("Response:");
            System.out.println(response.getChoices().get(0).getMessage().getContent());

            System.out.println("\n---");
            System.out.println("Tokens: " + response.getUsage().getTotalTokens());
            System.out.println("Cost: $" + String.format("%.4f", response.getUsage().getEstimatedCost()));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void interactiveMode() {
        System.out.println("WorldClass AI SDK - Interactive Mode");
        System.out.println("Type 'exit' to quit\n");

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) {
            System.err.println("Error: OPENAI_API_KEY environment variable not set");
            return;
        }

        LLMClient client = LLMClient.builder()
            .provider(LLMProvider.OPENAI)
            .apiKey(apiKey)
            .build();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine();

            if ("exit".equalsIgnoreCase(input.trim())) {
                break;
            }

            try {
                CompletionResponse response = client.complete(
                    CompletionRequest.builder()
                        .model("gpt-4")
                        .messages(List.of(
                            Message.builder()
                                .role(Message.Role.USER)
                                .content(input)
                                .build()
                        ))
                        .build()
                );

                System.out.println("AI: " + response.getChoices().get(0).getMessage().getContent());
                System.out.println();

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void printUsage() {
        System.out.println("WorldClass AI SDK - Command Line Interface");
        System.out.println("\nUsage:");
        System.out.println("  worldclass-ai chat <message>       - Send a message to GPT-4");
        System.out.println("  worldclass-ai interactive          - Start interactive mode");
        System.out.println("  worldclass-ai version              - Show version");
        System.out.println("  worldclass-ai help                 - Show this help");
        System.out.println("\nEnvironment Variables:");
        System.out.println("  OPENAI_API_KEY                     - OpenAI API key");
        System.out.println("  ANTHROPIC_API_KEY                  - Anthropic API key");
    }
}
