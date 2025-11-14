package com.worldclass.ai.core;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of LLMClientBuilder
 */
public class DefaultLLMClientBuilder implements LLMClientBuilder {
    private final LLMClientConfig config = LLMClientConfig.builder().build();
    private Map<String, String> headers = new HashMap<>();

    @Override
    public LLMClientBuilder provider(LLMProvider provider) {
        config.setProvider(provider);
        return this;
    }

    @Override
    public LLMClientBuilder apiKey(String apiKey) {
        config.setApiKey(apiKey);
        return this;
    }

    @Override
    public LLMClientBuilder baseUrl(String baseUrl) {
        config.setBaseUrl(baseUrl);
        return this;
    }

    @Override
    public LLMClientBuilder timeout(Duration timeout) {
        config.setTimeout(timeout);
        return this;
    }

    @Override
    public LLMClientBuilder connectTimeout(Duration connectTimeout) {
        config.setConnectTimeout(connectTimeout);
        return this;
    }

    @Override
    public LLMClientBuilder maxRetries(Integer maxRetries) {
        config.setMaxRetries(maxRetries);
        return this;
    }

    @Override
    public LLMClientBuilder resourceName(String resourceName) {
        config.setResourceName(resourceName);
        return this;
    }

    @Override
    public LLMClientBuilder region(String region) {
        config.setRegion(region);
        return this;
    }

    @Override
    public LLMClientBuilder projectId(String projectId) {
        config.setProjectId(projectId);
        return this;
    }

    @Override
    public LLMClientBuilder headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    @Override
    public LLMClientBuilder header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    @Override
    public LLMClientBuilder proxyHost(String proxyHost) {
        config.setProxyHost(proxyHost);
        return this;
    }

    @Override
    public LLMClientBuilder proxyPort(Integer proxyPort) {
        config.setProxyPort(proxyPort);
        return this;
    }

    @Override
    public LLMClientBuilder validateSsl(Boolean validateSsl) {
        config.setValidateSsl(validateSsl);
        return this;
    }

    @Override
    public LLMClient build() {
        config.setHeaders(headers);
        return new DefaultLLMClient(config);
    }
}
