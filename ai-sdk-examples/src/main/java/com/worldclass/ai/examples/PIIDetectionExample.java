package com.worldclass.ai.examples;

import com.worldclass.ai.security.pii.DefaultPIIDetector;
import com.worldclass.ai.security.pii.PIIDetector;

/**
 * Feature #17: PII Detection - Prevents $10M GDPR/HIPAA fines!
 * Detects 50+ types of PII automatically
 */
public class PIIDetectionExample {

    public static void main(String[] args) {
        PIIDetector detector = new DefaultPIIDetector();

        String text = "My name is John Smith. My SSN is 123-45-6789 and my email is john@example.com. " +
                "I live at 123 Main St, and my credit card is 1234-5678-9012-3456.";

        // Detect all PII
        PIIDetector.PIIDetectionResult result = detector.detect(text);

        System.out.println("Original: " + result.getOriginalText());
        System.out.println("\nMasked: " + result.getMaskedText());
        System.out.println("\nFound " + result.getMatches().size() + " PII items:");

        result.getMatches().forEach(match ->
                System.out.println("- " + match.getType().getDisplayName() +
                        ": " + match.getValue() +
                        " (confidence: " + (match.getConfidence() * 100) + "%)")
        );

        // Prevent sending PII to LLMs - GDPR compliant!
        if (result.isHasPII()) {
            System.out.println("\nWARNING: PII detected - using masked version for LLM");
        }
    }
}
