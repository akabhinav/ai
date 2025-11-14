package com.worldclass.ai.security.secrets;

import java.util.Map;
import java.util.Optional;

/**
 * Feature #23: Secrets Management - Secure API key storage
 *
 * Integrates with:
 * - HashiCorp Vault
 * - AWS Secrets Manager
 * - Azure Key Vault
 * - Google Secret Manager
 *
 * Features:
 * - Automatic key rotation
 * - Version management
 * - Audit logging
 * - Encryption at rest
 */
public interface SecretsManager {

    /**
     * Get secret value
     */
    Optional<String> getSecret(String key);

    /**
     * Store secret
     */
    void putSecret(String key, String value);

    /**
     * Delete secret
     */
    void deleteSecret(String key);

    /**
     * Rotate secret
     */
    void rotateSecret(String key);

    /**
     * Get secret metadata
     */
    SecretMetadata getMetadata(String key);

    /**
     * List all secret keys
     */
    Map<String, SecretMetadata> listSecrets();

    class SecretMetadata {
        public final String key;
        public final int version;
        public final long createdAt;
        public final long updatedAt;
        public final boolean rotationEnabled;

        public SecretMetadata(String key, int version, long createdAt, long updatedAt, boolean rotationEnabled) {
            this.key = key;
            this.version = version;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.rotationEnabled = rotationEnabled;
        }
    }
}
