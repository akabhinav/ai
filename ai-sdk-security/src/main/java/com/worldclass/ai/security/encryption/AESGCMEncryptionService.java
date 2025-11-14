package com.worldclass.ai.security.encryption;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feature #18: Military-grade AES-256-GCM encryption
 */
public class AESGCMEncryptionService implements EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int KEY_SIZE = 256;

    private final Map<String, SecretKey> keys = new ConcurrentHashMap<>();
    private volatile String currentKeyId;
    private final SecureRandom secureRandom = new SecureRandom();

    public AESGCMEncryptionService() {
        // Initialize with default key
        rotateKeys();
    }

    @Override
    public EncryptedData encrypt(byte[] plaintext) {
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Get current key
            SecretKey key = keys.get(currentKeyId);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext);

            // GCM mode appends auth tag to ciphertext
            // Split ciphertext and auth tag
            int ciphertextLength = ciphertext.length - (GCM_TAG_LENGTH / 8);
            byte[] actualCiphertext = new byte[ciphertextLength];
            byte[] authTag = new byte[GCM_TAG_LENGTH / 8];

            System.arraycopy(ciphertext, 0, actualCiphertext, 0, ciphertextLength);
            System.arraycopy(ciphertext, ciphertextLength, authTag, 0, authTag.length);

            return EncryptedData.builder()
                .ciphertext(actualCiphertext)
                .iv(iv)
                .authTag(authTag)
                .keyId(currentKeyId)
                .algorithm(ALGORITHM)
                .build();

        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(EncryptedData encrypted) {
        try {
            // Get key
            SecretKey key = keys.get(encrypted.getKeyId());
            if (key == null) {
                throw new EncryptionException("Key not found: " + encrypted.getKeyId());
            }

            // Combine ciphertext and auth tag
            byte[] combined = new byte[encrypted.getCiphertext().length + encrypted.getAuthTag().length];
            System.arraycopy(encrypted.getCiphertext(), 0, combined, 0, encrypted.getCiphertext().length);
            System.arraycopy(encrypted.getAuthTag(), 0, combined, encrypted.getCiphertext().length, encrypted.getAuthTag().length);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, encrypted.getIv());
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            // Decrypt and verify
            return cipher.doFinal(combined);

        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }

    @Override
    public String encryptString(String plaintext) {
        EncryptedData encrypted = encrypt(plaintext.getBytes(StandardCharsets.UTF_8));

        // Encode to base64 string for storage
        return Base64.getEncoder().encodeToString(encrypted.getCiphertext()) + ":" +
               Base64.getEncoder().encodeToString(encrypted.getIv()) + ":" +
               Base64.getEncoder().encodeToString(encrypted.getAuthTag()) + ":" +
               encrypted.getKeyId();
    }

    @Override
    public String decryptString(String ciphertext) {
        String[] parts = ciphertext.split(":");
        if (parts.length != 4) {
            throw new EncryptionException("Invalid encrypted string format");
        }

        EncryptedData encrypted = EncryptedData.builder()
            .ciphertext(Base64.getDecoder().decode(parts[0]))
            .iv(Base64.getDecoder().decode(parts[1]))
            .authTag(Base64.getDecoder().decode(parts[2]))
            .keyId(parts[3])
            .algorithm(ALGORITHM)
            .build();

        byte[] decrypted = decrypt(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    @Override
    public void rotateKeys() {
        try {
            // Generate new key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE, secureRandom);
            SecretKey newKey = keyGen.generateKey();

            // Store with timestamp-based ID
            String newKeyId = "key-" + System.currentTimeMillis();
            keys.put(newKeyId, newKey);
            currentKeyId = newKeyId;

            // Keep last 10 keys for decryption of old data
            if (keys.size() > 10) {
                String oldestKey = keys.keySet().stream()
                    .min(String::compareTo)
                    .orElse(null);
                if (oldestKey != null) {
                    keys.remove(oldestKey);
                }
            }

        } catch (Exception e) {
            throw new EncryptionException("Key rotation failed", e);
        }
    }

    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message) {
            super(message);
        }

        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
