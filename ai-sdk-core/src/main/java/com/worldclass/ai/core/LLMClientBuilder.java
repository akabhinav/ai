package com.worldclass.ai.core;

import java.time.Duration;
import java.util.Map;

/**
 * Builder for creating LLMClient instances
 */
public interface LLMClientBuilder {
    LLMClientBuilder provider(LLMProvider provider);
    LLMClientBuilder apiKey(String apiKey);
    LLMClientBuilder baseUrl(String baseUrl);
    LLMClientBuilder timeout(Duration timeout);
    LLMClientBuilder connectTimeout(Duration connectTimeout);
    LLMClientBuilder maxRetries(Integer maxRetries);
    LLMClientBuilder resourceName(String resourceName);
    LLMClientBuilder region(String region);
    LLMClientBuilder projectId(String projectId);
    LLMClientBuilder headers(Map<String, String> headers);
    LLMClientBuilder header(String key, String value);
    LLMClientBuilder proxyHost(String proxyHost);
    LLMClientBuilder proxyPort(Integer proxyPort);
    LLMClientBuilder validateSsl(Boolean validateSsl);
    LLMClient build();
}
