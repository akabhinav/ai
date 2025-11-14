package com.worldclass.ai.rag.memory;

import com.worldclass.ai.core.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Feature #29: Conversation Memory - Multi-turn context
 *
 * Manages conversation history with:
 * - Automatic context window management
 * - Conversation summarization
 * - Memory persistence
 * - Smart truncation
 */
public interface ConversationMemory {

    /**
     * Add message to conversation
     */
    void addMessage(String conversationId, Message message);

    /**
     * Get conversation history
     */
    List<Message> getHistory(String conversationId);

    /**
     * Get conversation history with token limit
     */
    List<Message> getHistory(String conversationId, int maxTokens);

    /**
     * Summarize and compress conversation
     */
    ConversationSummary summarize(String conversationId);

    /**
     * Clear conversation
     */
    void clear(String conversationId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ConversationSummary {
        private String conversationId;
        private String summary;
        private int originalMessageCount;
        private int originalTokens;
        private int summaryTokens;
    }
}
