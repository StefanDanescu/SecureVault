package com.securevault.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

/**
 * Provides encryption and decryption operations using AES-256-GCM.
 * 
 * AES-GCM provides authenticated encryption with associated data (AEAD),
 * which protects both confidentiality and integrity of the data.
 */
public class CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits - recommended for GCM
    private static final int GCM_TAG_LENGTH = 128; // 128 bits authentication tag

    private final SecureRandom secureRandom;

    public CryptoService() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     * 
     * @param plaintext The data to encrypt
     * @param key The AES-256 secret key
     * @return The IV prepended to the ciphertext
     * @throws Exception if encryption fails
     */
    public byte[] encrypt(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] ciphertext = cipher.doFinal(plaintext);

        // Prepend IV to ciphertext
        return ByteBuffer.allocate(iv.length + ciphertext.length)
                .put(iv)
                .put(ciphertext)
                .array();
    }

    /**
     * Decrypts ciphertext using AES-256-GCM.
     * 
     * @param ciphertextWithIv The IV prepended to the ciphertext
     * @param key The AES-256 secret key
     * @return The decrypted plaintext
     * @throws Exception if decryption fails or authentication fails
     */
    public byte[] decrypt(byte[] ciphertextWithIv, SecretKey key) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(ciphertextWithIv);

        byte[] iv = new byte[GCM_IV_LENGTH];
        buffer.get(iv);

        byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        return cipher.doFinal(ciphertext);
    }

    /**
     * Generates a random salt for key derivation.
     * 
     * @param length The length of the salt in bytes (recommended: 32)
     * @return A random salt
     */
    public byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
