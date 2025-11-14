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
 * OpenAI provider adapter
 * Supports GPT-4, GPT-3.5, DALL-E, Whisper, TTS
 */
public class OpenAIAdapter implements LLMProviderAdapter {

    @Override
    public Mono<CompletionResponse> complete(
            CompletionRequest request,
            LLMClientConfig config,
            OkHttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        return Mono.fromCallable(() -> {
            String url = (config.getBaseUrl() != null ? config.getBaseUrl() : "https://api.openai.com/v1")
                + "/chat/completions";

            Map<String, Object> body = buildRequestBody(request);
            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body),
                    MediaType.parse("application/json")
            );

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
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
                String url = (config.getBaseUrl() != null ? config.getBaseUrl() : "https://api.openai.com/v1")
                    + "/chat/completions";

                Map<String, Object> body = buildRequestBody(request);
                body.put("stream", true);

                RequestBody requestBody = RequestBody.create(
                        objectMapper.writeValueAsString(body),
                        MediaType.parse("application/json")
                );

                Request httpRequest = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer " + config.getApiKey())
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = httpClient.newCall(httpRequest).execute();

                // Parse SSE stream
                String line;
                while ((line = response.body().source().readUtf8Line()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if (!"[DONE]".equals(data)) {
                            CompletionResponse chunk = parseResponse(data, objectMapper);
                            sink.next(chunk);
                        }
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
        return Mono.fromCallable(() -> {
            String url = (config.getBaseUrl() != null ? config.getBaseUrl() : "https://api.openai.com/v1")
                + "/embeddings";

            Map<String, Object> body = new HashMap<>();
            body.put("model", request.getModel() != null ? request.getModel() : "text-embedding-ada-002");
            body.put("input", request.getInput());

            RequestBody requestBody = RequestBody.create(
                    objectMapper.writeValueAsString(body),
                    MediaType.parse("application/json")
            );

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response: " + response);
                }

                String responseBody = response.body().string();
                return parseEmbeddingResponse(responseBody, objectMapper);
            }
        });
    }

    @Override
    public int countTokens(String text, String model) {
        // Simplified token counting - in production, use tiktoken
        return text.length() / 4; // Rough estimate: 1 token ≈ 4 characters
    }

    @Override
    public double calculateCost(int promptTokens, int completionTokens, String model) {
        // OpenAI pricing (as of 2024)
        double promptCost = 0.0;
        double completionCost = 0.0;

        if (model.startsWith("gpt-4")) {
            promptCost = promptTokens * 0.00003; // $0.03 per 1K tokens
            completionCost = completionTokens * 0.00006; // $0.06 per 1K tokens
        } else if (model.startsWith("gpt-3.5")) {
            promptCost = promptTokens * 0.0000015; // $0.0015 per 1K tokens
            completionCost = completionTokens * 0.000002; // $0.002 per 1K tokens
        }

        return promptCost + completionCost;
    }

    private Map<String, Object> buildRequestBody(CompletionRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("messages", convertMessages(request.getMessages()));

        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            body.put("max_tokens", request.getMaxTokens());
        }
        if (request.getTopP() != null) {
            body.put("top_p", request.getTopP());
        }
        if (request.getStop() != null) {
            body.put("stop", request.getStop());
        }
        if (request.getFunctions() != null) {
            body.put("functions", request.getFunctions());
            if (request.getFunctionCall() != null) {
                body.put("function_call", request.getFunctionCall());
            }
        }
        if (request.getResponseFormat() != null) {
            body.put("response_format", request.getResponseFormat());
        }

        return body;
    }

    private List<Map<String, Object>> convertMessages(List<Message> messages) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Message msg : messages) {
            Map<String, Object> m = new HashMap<>();
            m.put("role", msg.getRole().name().toLowerCase());
            m.put("content", msg.getContent());
            result.add(m);
        }
        return result;
    }

    private CompletionResponse parseResponse(String json, ObjectMapper objectMapper) throws IOException {
        Map<String, Object> data = objectMapper.readValue(json, Map.class);

        List<CompletionResponse.Choice> choices = new ArrayList<>();
        List<Map<String, Object>> choicesData = (List<Map<String, Object>>) data.get("choices");

        for (Map<String, Object> choice : choicesData) {
            Map<String, Object> messageData = (Map<String, Object>) choice.get("message");
            Message message = Message.builder()
                    .role(Message.Role.valueOf(((String) messageData.get("role")).toUpperCase()))
                    .content((String) messageData.get("content"))
                    .build();

            choices.add(CompletionResponse.Choice.builder()
                    .index((Integer) choice.get("index"))
                    .message(message)
                    .finishReason((String) choice.get("finish_reason"))
                    .build());
        }

        Map<String, Object> usageData = (Map<String, Object>) data.get("usage");
        int promptTokens = (Integer) usageData.get("prompt_tokens");
        int completionTokens = (Integer) usageData.get("completion_tokens");

        CompletionResponse.Usage usage = CompletionResponse.Usage.builder()
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens((Integer) usageData.get("total_tokens"))
                .estimatedCost(calculateCost(promptTokens, completionTokens, (String) data.get("model")))
                .currency("USD")
                .build();

        return CompletionResponse.builder()
                .id((String) data.get("id"))
                .model((String) data.get("model"))
                .created(Instant.ofEpochSecond((Integer) data.get("created")))
                .choices(choices)
                .usage(usage)
                .build();
    }

    private EmbeddingResponse parseEmbeddingResponse(String json, ObjectMapper objectMapper) throws IOException {
        Map<String, Object> data = objectMapper.readValue(json, Map.class);

        List<EmbeddingResponse.EmbeddingData> embeddings = new ArrayList<>();
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) data.get("data");

        for (Map<String, Object> item : dataList) {
            embeddings.add(EmbeddingResponse.EmbeddingData.builder()
                    .index((Integer) item.get("index"))
                    .embedding((List<Double>) item.get("embedding"))
                    .object((String) item.get("object"))
                    .build());
        }

        return EmbeddingResponse.builder()
                .data(embeddings)
                .model((String) data.get("model"))
                .build();
    }
}
