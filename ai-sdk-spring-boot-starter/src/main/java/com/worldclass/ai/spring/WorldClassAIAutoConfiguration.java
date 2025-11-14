package com.worldclass.ai.spring;

import com.worldclass.ai.core.LLMClient;
import com.worldclass.ai.core.LLMProvider;
import com.worldclass.ai.optimization.cache.SmartCache;
import com.worldclass.ai.rag.RAGEngine;
import com.worldclass.ai.security.pii.DefaultPIIDetector;
import com.worldclass.ai.security.pii.PIIDetector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feature #41: Spring Boot Starter - Zero-config integration
 *
 * Add to application.yml:
 * <pre>
 * worldclass:
 *   ai:
 *     provider: OPENAI
 *     api-key: sk-...
 *     model: gpt-4
 *     cache:
 *       enabled: true
 *     rag:
 *       enabled: true
 * </pre>
 *
 * Then inject:
 * <pre>
 * {@literal @}Autowired
 * private LLMClient llmClient;
 *
 * {@literal @}Autowired
 * private RAGEngine ragEngine;
 * </pre>
 */
@Configuration
@EnableConfigurationProperties(WorldClassAIProperties.class)
public class WorldClassAIAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LLMClient llmClient(WorldClassAIProperties properties) {
        return LLMClient.builder()
                .provider(properties.getProvider())
                .apiKey(properties.getApiKey())
                .timeout(properties.getTimeout())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "worldclass.ai.cache.enabled", havingValue = "true")
    public SmartCache smartCache(LLMClient llmClient, WorldClassAIProperties properties) {
        return SmartCache.builder()
                .embeddingClient(llmClient)
                .maxSize(properties.getCache().getMaxSize())
                .ttl(properties.getCache().getTtl())
                .similarityThreshold(properties.getCache().getSimilarityThreshold())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "worldclass.ai.rag.enabled", havingValue = "true")
    public RAGEngine ragEngine(LLMClient llmClient, WorldClassAIProperties properties) {
        return RAGEngine.builder()
                .llmClient(llmClient)
                .embeddingClient(llmClient)
                .chunkSize(properties.getRag().getChunkSize())
                .topK(properties.getRag().getTopK())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public PIIDetector piiDetector() {
        return new DefaultPIIDetector();
    }
}
