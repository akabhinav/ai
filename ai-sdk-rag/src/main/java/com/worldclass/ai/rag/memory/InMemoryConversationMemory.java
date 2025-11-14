package com.worldclass.ai.rag.memory;

import com.worldclass.ai.core.model.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory conversation memory with automatic window management
 */
public class InMemoryConversationMemory implements ConversationMemory {

    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();
    private final int maxMessages;

    public InMemoryConversationMemory(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    @Override
    public void addMessage(String conversationId, Message message) {
        conversations.computeIfAbsent(conversationId, k -> new ArrayList<>())
            .add(message);

        // Auto-trim if exceeds max
        List<Message> messages = conversations.get(conversationId);
        if (messages.size() > maxMessages) {
            messages.subList(0, messages.size() - maxMessages).clear();
        }
    }

    @Override
    public List<Message> getHistory(String conversationId) {
        return new ArrayList<>(conversations.getOrDefault(conversationId, Collections.emptyList()));
    }

    @Override
    public List<Message> getHistory(String conversationId, int maxTokens) {
        List<Message> all = getHistory(conversationId);
        List<Message> result = new ArrayList<>();
        int tokens = 0;

        // Add messages from most recent, working backwards
        for (int i = all.size() - 1; i >= 0; i--) {
            Message msg = all.get(i);
            int msgTokens = estimateTokens(msg);

            if (tokens + msgTokens > maxTokens) {
                break;
            }

            result.add(0, msg);
            tokens += msgTokens;
        }

        return result;
    }

    @Override
    public ConversationSummary summarize(String conversationId) {
        List<Message> messages = getHistory(conversationId);

        int originalTokens = messages.stream()
            .mapToInt(this::estimateTokens)
            .sum();

        // Simple summarization - keep first and last messages
        List<Message> summarized = new ArrayList<>();
        if (!messages.isEmpty()) {
            summarized.add(messages.get(0));
            if (messages.size() > 1) {
                summarized.add(messages.get(messages.size() - 1));
            }
        }

        conversations.put(conversationId, summarized);

        int summaryTokens = summarized.stream()
            .mapToInt(this::estimateTokens)
            .sum();

        return ConversationSummary.builder()
            .conversationId(conversationId)
            .summary("Conversation summarized from " + messages.size() + " messages")
            .originalMessageCount(messages.size())
            .originalTokens(originalTokens)
            .summaryTokens(summaryTokens)
            .build();
    }

    @Override
    public void clear(String conversationId) {
        conversations.remove(conversationId);
    }

    private int estimateTokens(Message message) {
        return message.getContent() != null ? message.getContent().length() / 4 : 0;
    }
}
