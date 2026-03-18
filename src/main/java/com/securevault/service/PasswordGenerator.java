package com.securevault.service;

import java.security.SecureRandom;

/**
 * Secure password generation service.
 * 
 * Generates cryptographically secure random passwords
 * with configurable character sets and length.
 */
public class PasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    private final SecureRandom secureRandom;

    public PasswordGenerator() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generates a password with the specified options.
     * 
     * @param length The password length
     * @param includeUppercase Include uppercase letters
     * @param includeLowercase Include lowercase letters
     * @param includeNumbers Include numbers
     * @param includeSymbols Include symbols
     * @return A randomly generated password
     */
    public String generate(int length, boolean includeUppercase, boolean includeLowercase,
                          boolean includeNumbers, boolean includeSymbols) {
        
        StringBuilder charPool = new StringBuilder();

        if (includeUppercase) charPool.append(UPPERCASE);
        if (includeLowercase) charPool.append(LOWERCASE);
        if (includeNumbers) charPool.append(NUMBERS);
        if (includeSymbols) charPool.append(SYMBOLS);

        if (charPool.length() == 0) {
            // Default to lowercase if nothing selected
            charPool.append(LOWERCASE);
        }

        StringBuilder password = new StringBuilder(length);
        String pool = charPool.toString();

        // Ensure at least one character from each selected category
        if (includeUppercase) {
            password.append(UPPERCASE.charAt(secureRandom.nextInt(UPPERCASE.length())));
        }
        if (includeLowercase) {
            password.append(LOWERCASE.charAt(secureRandom.nextInt(LOWERCASE.length())));
        }
        if (includeNumbers) {
            password.append(NUMBERS.charAt(secureRandom.nextInt(NUMBERS.length())));
        }
        if (includeSymbols) {
            password.append(SYMBOLS.charAt(secureRandom.nextInt(SYMBOLS.length())));
        }

        // Fill remaining length with random characters from pool
        while (password.length() < length) {
            password.append(pool.charAt(secureRandom.nextInt(pool.length())));
        }

        // Shuffle the password to avoid predictable positions
        return shuffle(password.toString());
    }

    /**
     * Generates a password with default options (all character types).
     * 
     * @param length The password length
     * @return A randomly generated password
     */
    public String generate(int length) {
        return generate(length, true, true, true, true);
    }

    /**
     * Shuffles a string using Fisher-Yates algorithm.
     */
    private String shuffle(String str) {
        char[] chars = str.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}
