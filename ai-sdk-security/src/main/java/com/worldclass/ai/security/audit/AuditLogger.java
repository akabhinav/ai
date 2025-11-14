package com.worldclass.ai.security.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Feature #20: Audit Logging - Immutable compliance trail
 *
 * Immutable, tamper-proof audit logs for:
 * - Every API call
 * - Authentication events
 * - Authorization decisions
 * - Data access
 * - Configuration changes
 *
 * SOC 2, HIPAA, GDPR compliant
 */
public interface AuditLogger {

    /**
     * Log an audit event
     */
    void log(AuditEvent event);

    /**
     * Query audit logs
     */
    List<AuditEvent> query(AuditQuery query);

    /**
     * Verify log integrity
     */
    boolean verifyIntegrity();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class AuditEvent {
        private String id;
        private Instant timestamp;
        private EventType type;
        private String userId;
        private String action;
        private String resource;
        private Map<String, Object> metadata;
        private String ipAddress;
        private String userAgent;
        private String outcome; // SUCCESS, FAILURE, DENIED
        private String hash; // For tamper detection

        public enum EventType {
            AUTHENTICATION,
            AUTHORIZATION,
            API_CALL,
            DATA_ACCESS,
            CONFIG_CHANGE,
            SECURITY_EVENT
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class AuditQuery {
        private Instant startTime;
        private Instant endTime;
        private String userId;
        private AuditEvent.EventType eventType;
        private String resource;
        private int limit;
    }
}
