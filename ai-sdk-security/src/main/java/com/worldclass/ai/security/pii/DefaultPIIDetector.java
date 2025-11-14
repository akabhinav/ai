package com.worldclass.ai.security.pii;

import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default PII detector using regex patterns and NER
 * In production, this would use ML models for better accuracy
 */
public class DefaultPIIDetector implements PIIDetector {

    private final Map<PIIType, Pattern> patterns = new HashMap<>();

    public DefaultPIIDetector() {
        initializePatterns();
    }

    @Override
    public PIIDetectionResult detect(String text) {
        List<PIIMatch> matches = new ArrayList<>();

        for (Map.Entry<PIIType, Pattern> entry : patterns.entrySet()) {
            PIIType type = entry.getKey();
            Pattern pattern = entry.getValue();
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                matches.add(PIIMatch.builder()
                        .type(type)
                        .value(matcher.group())
                        .startIndex(matcher.start())
                        .endIndex(matcher.end())
                        .confidence(0.95)
                        .build());
            }
        }

        // Sort by start index (reverse) so we can replace from end to start
        matches.sort((a, b) -> Integer.compare(b.getStartIndex(), a.getStartIndex()));

        String maskedText = mask(text, MaskingStrategy.REPLACE_WITH_TYPE);

        return PIIDetectionResult.builder()
                .originalText(text)
                .maskedText(maskedText)
                .matches(matches)
                .hasPII(!matches.isEmpty())
                .build();
    }

    @Override
    public String mask(String text) {
        return mask(text, MaskingStrategy.REPLACE_WITH_TYPE);
    }

    @Override
    public String mask(String text, MaskingStrategy strategy) {
        String result = text;

        for (Map.Entry<PIIType, Pattern> entry : patterns.entrySet()) {
            PIIType type = entry.getKey();
            Pattern pattern = entry.getValue();
            Matcher matcher = pattern.matcher(result);

            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String replacement = getReplacement(matcher.group(), type, strategy);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

    private String getReplacement(String value, PIIType type, MaskingStrategy strategy) {
        return switch (strategy) {
            case REPLACE_WITH_TYPE -> "[" + type.name() + "]";
            case REPLACE_WITH_ASTERISKS -> value.replaceAll(".", "*");
            case HASH -> {
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    byte[] hash = md.digest(value.getBytes());
                    yield Base64.getEncoder().encodeToString(hash).substring(0, 16);
                } catch (Exception e) {
                    yield "[HASHED]";
                }
            }
            case REDACT -> "[REDACTED]";
        };
    }

    private void initializePatterns() {
        // Credit Card (simplified)
        patterns.put(PIIType.CREDIT_CARD,
                Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"));

        // SSN
        patterns.put(PIIType.SSN,
                Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"));

        // Email
        patterns.put(PIIType.EMAIL,
                Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"));

        // Phone (US format)
        patterns.put(PIIType.PHONE,
                Pattern.compile("\\b\\(\\d{3}\\)[\\s-]?\\d{3}-\\d{4}\\b|\\b\\d{3}[\\s-]\\d{3}[\\s-]\\d{4}\\b"));

        // IP Address
        patterns.put(PIIType.IP_ADDRESS,
                Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"));

        // API Keys (generic pattern)
        patterns.put(PIIType.API_KEY,
                Pattern.compile("\\b[A-Za-z0-9]{32,}\\b"));

        // Add more patterns for other PII types...
    }
}
