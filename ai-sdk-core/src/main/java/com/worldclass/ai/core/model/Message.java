package com.worldclass.ai.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified message format across all providers
 * Supports Feature #3: Multi-Modal Support (text, images, audio, video)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private Role role;
    private String content;

    @Builder.Default
    private List<Content> contents = new ArrayList<>();

    private String name;
    private FunctionCall functionCall;
    private String toolCallId;

    public enum Role {
        SYSTEM,
        USER,
        ASSISTANT,
        FUNCTION
    }

    /**
     * Multi-modal content support
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private ContentType type;
        private String text;
        private MediaContent media;

        public enum ContentType {
            TEXT,
            IMAGE,
            AUDIO,
            VIDEO,
            FILE
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaContent {
        private String url;
        private byte[] data;
        private String mimeType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionCall {
        private String name;
        private String arguments;
    }
}
