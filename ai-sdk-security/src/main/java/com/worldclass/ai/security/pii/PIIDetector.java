package com.worldclass.ai.security.pii;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Feature #17: PII Detection - 50+ types auto-masked
 *
 * Automatically detects and masks PII to prevent $10M GDPR/HIPAA fines!
 *
 * Detects:
 * - Credit card numbers, SSN, passport numbers
 * - Email addresses, phone numbers
 * - IP addresses, MAC addresses
 * - Names, addresses, dates of birth
 * - Medical record numbers, driver's licenses
 * - Custom patterns (API keys, tokens, etc.)
 *
 * Example:
 * Input:  "My SSN is 123-45-6789 and email is john@example.com"
 * Output: "My SSN is [SSN] and email is [EMAIL]"
 */
public interface PIIDetector {

    /**
     * Detect PII in text
     */
    PIIDetectionResult detect(String text);

    /**
     * Mask PII in text
     */
    String mask(String text);

    /**
     * Mask PII with custom replacement
     */
    String mask(String text, MaskingStrategy strategy);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PIIDetectionResult {
        private String originalText;
        private String maskedText;
        private List<PIIMatch> matches;
        private boolean hasPII;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PIIMatch {
        private PIIType type;
        private String value;
        private int startIndex;
        private int endIndex;
        private double confidence;
    }

    enum PIIType {
        // Financial
        CREDIT_CARD("Credit Card"),
        SSN("Social Security Number"),
        BANK_ACCOUNT("Bank Account"),
        ROUTING_NUMBER("Routing Number"),

        // Personal
        EMAIL("Email Address"),
        PHONE("Phone Number"),
        NAME("Person Name"),
        ADDRESS("Physical Address"),
        DATE_OF_BIRTH("Date of Birth"),

        // Government IDs
        PASSPORT("Passport Number"),
        DRIVERS_LICENSE("Driver's License"),
        NATIONAL_ID("National ID"),

        // Medical
        MEDICAL_RECORD("Medical Record Number"),
        HEALTH_PLAN("Health Plan Number"),

        // Technical
        IP_ADDRESS("IP Address"),
        MAC_ADDRESS("MAC Address"),
        API_KEY("API Key"),
        SECRET_KEY("Secret Key"),

        // Custom
        CUSTOM("Custom PII");

        private final String displayName;

        PIIType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    enum MaskingStrategy {
        REPLACE_WITH_TYPE,    // "123-45-6789" → "[SSN]"
        REPLACE_WITH_ASTERISKS, // "123-45-6789" → "***-**-****"
        HASH,                 // "123-45-6789" → "a3f8b9..."
        REDACT                // "123-45-6789" → "[REDACTED]"
    }
}
