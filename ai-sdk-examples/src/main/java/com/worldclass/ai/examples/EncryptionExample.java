package com.worldclass.ai.examples;

import com.worldclass.ai.security.encryption.AESGCMEncryptionService;
import com.worldclass.ai.security.encryption.EncryptionService;

/**
 * Feature #18: Encryption - Military-grade AES-256-GCM
 */
public class EncryptionExample {

    public static void main(String[] args) {
        EncryptionService encryption = new AESGCMEncryptionService();

        // Sensitive data (e.g., API keys, user data)
        String apiKey = "sk-very-secret-api-key-12345";
        String userData = "SSN: 123-45-6789, Email: user@example.com";

        System.out.println("=== Encrypting Sensitive Data ===\n");

        // Encrypt
        String encryptedKey = encryption.encryptString(apiKey);
        String encryptedData = encryption.encryptString(userData);

        System.out.println("Original API Key: " + apiKey);
        System.out.println("Encrypted: " + encryptedKey.substring(0, 50) + "...");

        System.out.println("\nOriginal User Data: " + userData);
        System.out.println("Encrypted: " + encryptedData.substring(0, 50) + "...");

        // Decrypt
        String decryptedKey = encryption.decryptString(encryptedKey);
        String decryptedData = encryption.decryptString(encryptedData);

        System.out.println("\n=== Decrypted ===");
        System.out.println("API Key: " + decryptedKey);
        System.out.println("User Data: " + decryptedData);

        // Verify
        System.out.println("\n=== Verification ===");
        System.out.println("API Key Match: " + apiKey.equals(decryptedKey));
        System.out.println("User Data Match: " + userData.equals(decryptedData));

        // Key rotation
        System.out.println("\n=== Key Rotation ===");
        encryption.rotateKeys();
        System.out.println("Encryption keys rotated successfully");
        System.out.println("Old encrypted data can still be decrypted:");
        System.out.println("Decrypted API Key: " + encryption.decryptString(encryptedKey));
    }
}
