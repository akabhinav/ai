package com.worldclass.ai.core.prompt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing prompt templates
 * Supports versioning and lookup
 */
public class PromptTemplateRegistry {
    private final Map<String, Map<String, PromptTemplate>> templates = new ConcurrentHashMap<>();

    /**
     * Register a prompt template
     */
    public void register(PromptTemplate template) {
        templates
                .computeIfAbsent(template.getName(), k -> new ConcurrentHashMap<>())
                .put(template.getVersion(), template);
    }

    /**
     * Get a specific version of a template
     */
    public PromptTemplate get(String name, String version) {
        Map<String, PromptTemplate> versions = templates.get(name);
        if (versions == null) {
            throw new IllegalArgumentException("Template not found: " + name);
        }

        PromptTemplate template = versions.get(version);
        if (template == null) {
            throw new IllegalArgumentException("Template version not found: " + name + ":" + version);
        }

        return template;
    }

    /**
     * Get the latest version of a template
     */
    public PromptTemplate getLatest(String name) {
        Map<String, PromptTemplate> versions = templates.get(name);
        if (versions == null || versions.isEmpty()) {
            throw new IllegalArgumentException("Template not found: " + name);
        }

        // Return the latest version (highest version number)
        return versions.values().stream()
                .max((t1, t2) -> compareVersions(t1.getVersion(), t2.getVersion()))
                .orElseThrow();
    }

    /**
     * List all templates
     */
    public Map<String, Map<String, PromptTemplate>> listAll() {
        return new ConcurrentHashMap<>(templates);
    }

    /**
     * Remove a template
     */
    public void remove(String name, String version) {
        Map<String, PromptTemplate> versions = templates.get(name);
        if (versions != null) {
            versions.remove(version);
            if (versions.isEmpty()) {
                templates.remove(name);
            }
        }
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
        }

        return 0;
    }
}
