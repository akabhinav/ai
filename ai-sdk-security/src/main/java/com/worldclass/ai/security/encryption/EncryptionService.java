package com.worldclass.ai.security.encryption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Feature #18: Encryption - Military-grade everywhere
 *
 * AES-256-GCM encryption for:
 * - Data at rest
 * - Data in transit (TLS 1.3)
 * - Prompts and responses
 * - API keys and secrets
 * - Automatic key rotation
 */
public interface EncryptionService {

    /**
     * Encrypt data
     */
    EncryptedData encrypt(byte[] plaintext);

    /**
     * Decrypt data
     */
    byte[] decrypt(EncryptedData encrypted);

    /**
     * Encrypt string
     */
    String encryptString(String plaintext);

    /**
     * Decrypt string
     */
    String decryptString(String ciphertext);

    /**
     * Rotate encryption keys
     */
    void rotateKeys();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class EncryptedData {
        private byte[] ciphertext;
        private byte[] iv;
        private byte[] authTag;
        private String keyId;
        private String algorithm;
    }
}
