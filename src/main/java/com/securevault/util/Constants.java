package com.securevault.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Application-wide constants.
 */
public final class Constants {

    private Constants() {
        // Utility class - prevent instantiation
    }

    // Application info
    public static final String APP_NAME = "SecureVault";
    public static final String APP_VERSION = "1.0.0";

    // Vault file
    public static final int VAULT_VERSION = 1;
    public static final String VAULT_EXTENSION = ".vault";
    public static final Path DEFAULT_VAULT_PATH = Paths.get(
        System.getProperty("user.home"), ".securevault", "default.vault"
    );

    // Cryptographic constants
    public static final int SALT_LENGTH = 32; // 256 bits
    public static final int KEY_LENGTH = 256; // AES-256
    public static final int DEFAULT_ITERATIONS = 600_000; // OWASP 2023

    // UI constants
    public static final int MIN_WINDOW_WIDTH = 800;
    public static final int MIN_WINDOW_HEIGHT = 500;
    public static final int DEFAULT_WINDOW_WIDTH = 1280;
    public static final int DEFAULT_WINDOW_HEIGHT = 720;

    // Security defaults
    public static final int DEFAULT_AUTO_LOCK_MINUTES = 5;
    public static final int DEFAULT_CLIPBOARD_CLEAR_SECONDS = 30;
    public static final int MIN_PASSWORD_LENGTH = 8;
}
