package com.securevault.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CryptoService.
 */
class CryptoServiceTest {

    private CryptoService cryptoService;
    private KeyDerivation keyDerivation;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService();
        keyDerivation = new KeyDerivation();
    }

    @Test
    void testEncryptDecrypt() throws Exception {
        // Arrange
        String original = "Hello, SecureVault!";
        byte[] plaintext = original.getBytes(StandardCharsets.UTF_8);
        byte[] salt = cryptoService.generateSalt(32);
        SecretKey key = keyDerivation.deriveKey("testPassword123".toCharArray(), salt);

        // Act
        byte[] encrypted = cryptoService.encrypt(plaintext, key);
        byte[] decrypted = cryptoService.decrypt(encrypted, key);

        // Assert
        assertNotNull(encrypted);
        assertTrue(encrypted.length > plaintext.length); // IV + ciphertext + tag
        assertEquals(original, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    void testEncryptProducesDifferentOutput() throws Exception {
        // Arrange
        String original = "Same plaintext";
        byte[] plaintext = original.getBytes(StandardCharsets.UTF_8);
        byte[] salt = cryptoService.generateSalt(32);
        SecretKey key = keyDerivation.deriveKey("testPassword123".toCharArray(), salt);

        // Act - encrypt same plaintext twice
        byte[] encrypted1 = cryptoService.encrypt(plaintext, key);
        byte[] encrypted2 = cryptoService.encrypt(plaintext, key);

        // Assert - should produce different ciphertext due to random IV
        assertFalse(java.util.Arrays.equals(encrypted1, encrypted2));
    }

    @Test
    void testWrongKeyFailsDecrypt() throws Exception {
        // Arrange
        String original = "Secret data";
        byte[] plaintext = original.getBytes(StandardCharsets.UTF_8);
        byte[] salt = cryptoService.generateSalt(32);
        SecretKey correctKey = keyDerivation.deriveKey("correctPassword".toCharArray(), salt);
        SecretKey wrongKey = keyDerivation.deriveKey("wrongPassword".toCharArray(), salt);

        // Act
        byte[] encrypted = cryptoService.encrypt(plaintext, correctKey);

        // Assert
        assertThrows(Exception.class, () -> {
            cryptoService.decrypt(encrypted, wrongKey);
        });
    }

    @Test
    void testGenerateSalt() {
        // Act
        byte[] salt1 = cryptoService.generateSalt(32);
        byte[] salt2 = cryptoService.generateSalt(32);

        // Assert
        assertEquals(32, salt1.length);
        assertEquals(32, salt2.length);
        assertFalse(java.util.Arrays.equals(salt1, salt2));
    }
}
