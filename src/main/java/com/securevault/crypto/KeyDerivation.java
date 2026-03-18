package com.securevault.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;

/**
 * Handles key derivation from master password using PBKDF2-HMAC-SHA256.
 * 
 * Uses 600,000 iterations as recommended by OWASP 2023 guidelines
 * for password-based key derivation.
 */
public class KeyDerivation {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 600_000; // OWASP 2023 recommendation
    private static final int KEY_LENGTH = 256; // AES-256

    /**
     * Derives an AES-256 key from a master password and salt.
     * 
     * @param password The master password
     * @param salt The random salt (should be at least 32 bytes)
     * @return A 256-bit AES secret key
     * @throws Exception if key derivation fails
     */
    public SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        return deriveKey(password, salt, DEFAULT_ITERATIONS);
    }

    /**
     * Derives an AES-256 key from a master password, salt, and iteration count.
     * 
     * @param password The master password
     * @param salt The random salt (should be at least 32 bytes)
     * @param iterations The number of PBKDF2 iterations
     * @return A 256-bit AES secret key
     * @throws Exception if key derivation fails
     */
    public SecretKey deriveKey(char[] password, byte[] salt, int iterations) throws Exception {
        KeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Returns the default number of iterations used for key derivation.
     * 
     * @return The default iteration count
     */
    public int getDefaultIterations() {
        return DEFAULT_ITERATIONS;
    }
}
