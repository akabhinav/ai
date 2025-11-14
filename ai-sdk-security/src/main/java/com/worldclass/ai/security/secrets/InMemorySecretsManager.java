package com.worldclass.ai.security.secrets;

import com.worldclass.ai.security.encryption.AESGCMEncryptionService;
import com.worldclass.ai.security.encryption.EncryptionService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory secrets manager with encryption
 * Production should use Vault, AWS Secrets Manager, etc.
 */
public class InMemorySecretsManager implements SecretsManager {

    private final Map<String, SecretEntry> secrets = new ConcurrentHashMap<>();
    private final EncryptionService encryption;

    public InMemorySecretsManager() {
        this.encryption = new AESGCMEncryptionService();
    }

    public InMemorySecretsManager(EncryptionService encryption) {
        this.encryption = encryption;
    }

    @Override
    public Optional<String> getSecret(String key) {
        SecretEntry entry = secrets.get(key);
        if (entry == null) {
            return Optional.empty();
        }

        // Decrypt value
        String decrypted = encryption.decryptString(entry.encryptedValue);
        return Optional.of(decrypted);
    }

    @Override
    public void putSecret(String key, String value) {
        // Encrypt value
        String encrypted = encryption.encryptString(value);

        SecretEntry existing = secrets.get(key);
        int version = existing != null ? existing.version + 1 : 1;

        SecretEntry entry = new SecretEntry(
            encrypted,
            version,
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            false
        );

        secrets.put(key, entry);
    }

    @Override
    public void deleteSecret(String key) {
        secrets.remove(key);
    }

    @Override
    public void rotateSecret(String key) {
        Optional<String> current = getSecret(key);
        if (current.isPresent()) {
            // In production, would generate new secret
            // For now, just re-encrypt with new key
            encryption.rotateKeys();
            putSecret(key, current.get());
        }
    }

    @Override
    public SecretMetadata getMetadata(String key) {
        SecretEntry entry = secrets.get(key);
        if (entry == null) {
            return null;
        }

        return new SecretMetadata(
            key,
            entry.version,
            entry.createdAt,
            entry.updatedAt,
            entry.rotationEnabled
        );
    }

    @Override
    public Map<String, SecretMetadata> listSecrets() {
        Map<String, SecretMetadata> result = new HashMap<>();
        for (Map.Entry<String, SecretEntry> entry : secrets.entrySet()) {
            result.put(entry.getKey(), getMetadata(entry.getKey()));
        }
        return result;
    }

    private static class SecretEntry {
        final String encryptedValue;
        final int version;
        final long createdAt;
        final long updatedAt;
        final boolean rotationEnabled;

        SecretEntry(String encryptedValue, int version, long createdAt, long updatedAt, boolean rotationEnabled) {
            this.encryptedValue = encryptedValue;
            this.version = version;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.rotationEnabled = rotationEnabled;
        }
    }
}
