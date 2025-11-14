package com.worldclass.ai.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Map;

/**
 * Configuration for LLM client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMClientConfig {
    private LLMProvider provider;
    private String apiKey;
    private String baseUrl;

    @Builder.Default
    private Duration timeout = Duration.ofSeconds(60);

    @Builder.Default
    private Duration connectTimeout = Duration.ofSeconds(10);

    @Builder.Default
    private Integer maxRetries = 3;

    // For Azure, AWS, GCP
    private String resourceName;
    private String region;
    private String projectId;

    // Custom headers
    private Map<String, String> headers;

    // Proxy settings
    private String proxyHost;
    private Integer proxyPort;

    @Builder.Default
    private Boolean validateSsl = true;
}
