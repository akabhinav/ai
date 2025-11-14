package com.worldclass.ai.core.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldclass.ai.core.LLMClientConfig;
import com.worldclass.ai.core.model.*;
import okhttp3.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * Anthropic (Claude) provider adapter
 * Supports Claude 3 Opus, Sonnet, Haiku
 */
public class AnthropicAdapter implements LLMProviderAdapter {

    @Override
    public Mono<CompletionResponse> complete(
            CompletionRequest request,
            LLMClientConfig config,
            OkHttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        return Mono.fromCallable(() -> {
            String url = (config.getBaseUrl() != null ? config.getBaseUrl() : "https://api.anthropic.com/v1")
                + "/messages";

            Map<String, Object> body = buildRequestBody(request);
            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body),
                    MediaType.parse("application/json")
            );

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("x-api-key", config.getApiKey())
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response: " + response);
                }

                String responseBody = response.body().string();
                return parseResponse(responseBody, objectMapper);
            }
        });
    }

    @Override
    public Flux<CompletionResponse> completeStream(
            CompletionRequest request,
            LLMClientConfig config,
            OkHttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        return Flux.create(sink -> {
            try {
                String url = (config.getBaseUrl() != null ? config.getBaseUrl() : "https://api.anthropic.com/v1")
                    + "/messages";

                Map<String, Object> body = buildRequestBody(request);
                body.put("stream", true);

                RequestBody requestBody = RequestBody.create(
                        objectMapper.writeValueAsString(body),
                        MediaType.parse("application/json")
                );

                Request httpRequest = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .addHeader("x-api-key", config.getApiKey())
                        .addHeader("anthropic-version", "2023-06-01")
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = httpClient.newCall(httpRequest).execute();

                String line;
                while ((line = response.body().source().readUtf8Line()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        CompletionResponse chunk = parseResponse(data, objectMapper);
                        sink.next(chunk);
                    }
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public Mono<EmbeddingResponse> embed(
            EmbeddingRequest request,
            LLMClientConfig config,
            OkHttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        // Anthropic doesn't provide embeddings, use Voyage AI or similar
        return Mono.error(new UnsupportedOperationException("Anthropic does not support embeddings"));
    }

    @Override
    public int countTokens(String text, String model) {
        return text.length() / 4;
    }

    @Override
    public double calculateCost(int promptTokens, int completionTokens, String model) {
        double promptCost = 0.0;
        double completionCost = 0.0;

        if (model.contains("opus")) {
            promptCost = promptTokens * 0.000015;
            completionCost = completionTokens * 0.000075;
        } else if (model.contains("sonnet")) {
            promptCost = promptTokens * 0.000003;
            completionCost = completionTokens * 0.000015;
        } else if (model.contains("haiku")) {
            promptCost = promptTokens * 0.00000025;
            completionCost = completionTokens * 0.00000125;
        }

        return promptCost + completionCost;
    }

    private Map<String, Object> buildRequestBody(CompletionRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 4096);

        // Extract system message
        String systemMessage = null;
        List<Map<String, Object>> messages = new ArrayList<>();

        for (Message msg : request.getMessages()) {
            if (msg.getRole() == Message.Role.SYSTEM) {
                systemMessage = msg.getContent();
            } else {
                Map<String, Object> m = new HashMap<>();
                m.put("role", msg.getRole().name().toLowerCase());
                m.put("content", msg.getContent());
                messages.add(m);
            }
        }

        if (systemMessage != null) {
            body.put("system", systemMessage);
        }
        body.put("messages", messages);

        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        }
        if (request.getTopP() != null) {
            body.put("top_p", request.getTopP());
        }
        if (request.getTopK() != null) {
            body.put("top_k", request.getTopK());
        }

        return body;
    }

    private CompletionResponse parseResponse(String json, ObjectMapper objectMapper) throws IOException {
        Map<String, Object> data = objectMapper.readValue(json, Map.class);

        List<Map<String, Object>> contentList = (List<Map<String, Object>>) data.get("content");
        String content = (String) contentList.get(0).get("text");

        Message message = Message.builder()
                .role(Message.Role.ASSISTANT)
                .content(content)
                .build();

        Map<String, Object> usageData = (Map<String, Object>) data.get("usage");
        int inputTokens = (Integer) usageData.get("input_tokens");
        int outputTokens = (Integer) usageData.get("output_tokens");

        CompletionResponse.Usage usage = CompletionResponse.Usage.builder()
                .promptTokens(inputTokens)
                .completionTokens(outputTokens)
                .totalTokens(inputTokens + outputTokens)
                .estimatedCost(calculateCost(inputTokens, outputTokens, (String) data.get("model")))
                .currency("USD")
                .build();

        return CompletionResponse.builder()
                .id((String) data.get("id"))
                .model((String) data.get("model"))
                .created(Instant.now())
                .choices(List.of(CompletionResponse.Choice.builder()
                        .index(0)
                        .message(message)
                        .finishReason((String) data.get("stop_reason"))
                        .build()))
                .usage(usage)
                .build();
    }
}
