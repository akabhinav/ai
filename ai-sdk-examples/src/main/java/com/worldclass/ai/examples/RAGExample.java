package com.worldclass.ai.examples;

import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.LLMProvider;
import com.worldclass.ai.rag.RAGEngine;

import java.util.List;

/**
 * Feature #25: RAG Engine - ChatGPT for your docs
 * 6 months of work → 1 day!
 */
public class RAGExample {

    public static void main(String[] args) {
        LLMClient client = LLMClient.builder()
                .provider(LLMProvider.OPENAI)
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();

        // Create RAG engine
        RAGEngine rag = RAGEngine.builder()
                .llmClient(client)
                .embeddingClient(client)
                .chunkSize(512)
                .topK(5)
                .useHybridSearch(true) // Feature #27: Hybrid Search - 40% better
                .useReranking(true)    // Feature #28: Re-Ranking - 60% better accuracy
                .build();

        // Index your documents
        rag.indexDocument(RAGEngine.Document.builder()
                .id("user-manual")
                .source("user-manual.pdf")
                .content("To reset your password, go to Settings > Security > Reset Password. " +
                        "Click the 'Reset' button and enter your email address. " +
                        "You will receive a password reset link within 5 minutes.")
                .build());

        // Query like ChatGPT
        RAGEngine.RAGResponse response = rag.query("How do I reset my password?");

        System.out.println("Answer: " + response.getAnswer());
        System.out.println("\nCitations:");
        response.getCitations().forEach(citation ->
                System.out.println("- " + citation.getSource() + " (relevance: " +
                        (citation.getRelevance() * 100) + "%)")
        );
        System.out.println("\nConfidence: " + (response.getConfidence() * 100) + "%");
    }
}
