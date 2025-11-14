package com.worldclass.ai.spring;

import com.worldclass.ai.core.LLMProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for WorldClass AI SDK
 */
@Data
@ConfigurationProperties(prefix = "worldclass.ai")
public class WorldClassAIProperties {
    private LLMProvider provider = LLMProvider.OPENAI;
    private String apiKey;
    private String model = "gpt-4";
    private Duration timeout = Duration.ofSeconds(60);

    private CacheProperties cache = new CacheProperties();
    private RAGProperties rag = new RAGProperties();
    private SecurityProperties security = new SecurityProperties();

    @Data
    public static class CacheProperties {
        private boolean enabled = false;
        private long maxSize = 10000;
        private Duration ttl = Duration.ofHours(24);
        private double similarityThreshold = 0.95;
    }

    @Data
    public static class RAGProperties {
        private boolean enabled = false;
        private int chunkSize = 512;
        private int topK = 5;
    }

    @Data
    public static class SecurityProperties {
        private boolean piiDetection = false;
        private boolean encryption = false;
    }
}
