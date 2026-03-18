package com.securevault.crypto;

import java.util.Arrays;

/**
 * Utility class for secure handling of sensitive data in memory.
 * 
 * Provides methods to clear sensitive data from memory after use
 * to minimize the window of exposure for sensitive information.
 */
public final class SecureMemory {

    private SecureMemory() {
        // Utility class - prevent instantiation
    }

    /**
     * Securely clears a character array by overwriting with zeros.
     * 
     * @param chars The character array to clear (may be null)
     */
    public static void clear(char[] chars) {
        if (chars != null) {
            Arrays.fill(chars, '\0');
        }
    }

    /**
     * Securely clears a byte array by overwriting with zeros.
     * 
     * @param bytes The byte array to clear (may be null)
     */
    public static void clear(byte[] bytes) {
        if (bytes != null) {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Converts a char array to a byte array using UTF-8 encoding.
     * Note: The caller is responsible for clearing the returned byte array.
     * 
     * @param chars The character array to convert
     * @return A byte array representation
     */
    public static byte[] toBytes(char[] chars) {
        if (chars == null) {
            return new byte[0];
        }
        byte[] bytes = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; i++) {
            bytes[i * 2] = (byte) (chars[i] >> 8);
            bytes[i * 2 + 1] = (byte) chars[i];
        }
        return bytes;
    }

    /**
     * Creates a copy of a char array.
     * Note: The caller is responsible for clearing the returned array.
     * 
     * @param chars The character array to copy
     * @return A copy of the array, or null if input is null
     */
    public static char[] copy(char[] chars) {
        if (chars == null) {
            return null;
        }
        return Arrays.copyOf(chars, chars.length);
    }
}
