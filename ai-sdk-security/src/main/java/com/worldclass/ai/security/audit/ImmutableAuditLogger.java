package com.worldclass.ai.security.audit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Immutable audit logger with tamper detection
 */
public class ImmutableAuditLogger implements AuditLogger {

    private final Queue<AuditEvent> events = new ConcurrentLinkedQueue<>();
    private volatile String lastHash = "0";

    @Override
    public void log(AuditEvent event) {
        // Set timestamp
        if (event.getTimestamp() == null) {
            event.setTimestamp(Instant.now());
        }

        // Generate ID
        if (event.getId() == null) {
            event.setId(UUID.randomUUID().toString());
        }

        // Calculate hash (chain to previous hash for integrity)
        String hash = calculateHash(event, lastHash);
        event.setHash(hash);
        lastHash = hash;

        // Store (immutable)
        events.add(event);
    }

    @Override
    public List<AuditEvent> query(AuditQuery query) {
        return events.stream()
            .filter(event -> matchesQuery(event, query))
            .limit(query.getLimit() > 0 ? query.getLimit() : Long.MAX_VALUE)
            .collect(Collectors.toList());
    }

    @Override
    public boolean verifyIntegrity() {
        String previousHash = "0";

        for (AuditEvent event : events) {
            String expectedHash = calculateHash(event, previousHash);
            if (!expectedHash.equals(event.getHash())) {
                return false; // Tampering detected!
            }
            previousHash = event.getHash();
        }

        return true;
    }

    private String calculateHash(AuditEvent event, String previousHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Include previous hash (blockchain-style chaining)
            digest.update(previousHash.getBytes(StandardCharsets.UTF_8));

            // Include event data
            digest.update(event.getTimestamp().toString().getBytes(StandardCharsets.UTF_8));
            digest.update(event.getType().name().getBytes(StandardCharsets.UTF_8));
            if (event.getUserId() != null) {
                digest.update(event.getUserId().getBytes(StandardCharsets.UTF_8));
            }
            if (event.getAction() != null) {
                digest.update(event.getAction().getBytes(StandardCharsets.UTF_8));
            }
            if (event.getResource() != null) {
                digest.update(event.getResource().getBytes(StandardCharsets.UTF_8));
            }

            byte[] hashBytes = digest.digest();
            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (Exception e) {
            throw new RuntimeException("Hash calculation failed", e);
        }
    }

    private boolean matchesQuery(AuditEvent event, AuditQuery query) {
        if (query.getStartTime() != null && event.getTimestamp().isBefore(query.getStartTime())) {
            return false;
        }
        if (query.getEndTime() != null && event.getTimestamp().isAfter(query.getEndTime())) {
            return false;
        }
        if (query.getUserId() != null && !query.getUserId().equals(event.getUserId())) {
            return false;
        }
        if (query.getEventType() != null && query.getEventType() != event.getType()) {
            return false;
        }
        if (query.getResource() != null && !query.getResource().equals(event.getResource())) {
            return false;
        }
        return true;
    }
}
