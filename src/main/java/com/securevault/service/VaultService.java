package com.securevault.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.securevault.crypto.CryptoService;
import com.securevault.crypto.KeyDerivation;
import com.securevault.crypto.SecureMemory;
import com.securevault.model.Vault;
import com.securevault.util.Constants;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for vault CRUD operations including encryption and persistence.
 * 
 * Handles loading, saving, creating, and unlocking vaults with
 * proper encryption using the master password.
 */
public class VaultService {

    private final CryptoService cryptoService;
    private final KeyDerivation keyDerivation;
    private final ObjectMapper objectMapper;

    private Vault currentVault;
    private SecretKey currentKey;
    private Path currentVaultPath;

    public VaultService() {
        this.cryptoService = new CryptoService();
        this.keyDerivation = new KeyDerivation();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Creates a new vault with the given master password.
     * 
     * @param vaultPath Path where the vault file will be saved
     * @param masterPassword The master password for the vault
     * @throws Exception if vault creation fails
     */
    public void createVault(Path vaultPath, char[] masterPassword) throws Exception {
        byte[] salt = cryptoService.generateSalt(Constants.SALT_LENGTH);
        SecretKey key = keyDerivation.deriveKey(masterPassword, salt);

        this.currentVault = new Vault();
        this.currentKey = key;
        this.currentVaultPath = vaultPath;

        saveVault(salt, keyDerivation.getDefaultIterations());
    }

    /**
     * Opens an existing vault with the given master password.
     * 
     * @param vaultPath Path to the vault file
     * @param masterPassword The master password for the vault
     * @throws Exception if vault cannot be opened or password is incorrect
     */
    public void openVault(Path vaultPath, char[] masterPassword) throws Exception {
        String content = Files.readString(vaultPath, StandardCharsets.UTF_8);
        Map<String, Object> vaultFile = objectMapper.readValue(content, Map.class);

        byte[] salt = Base64.getDecoder().decode((String) vaultFile.get("salt"));
        int iterations = (int) vaultFile.get("iterations");
        byte[] encryptedData = Base64.getDecoder().decode((String) vaultFile.get("encryptedData"));

        SecretKey key = keyDerivation.deriveKey(masterPassword, salt, iterations);

        byte[] decrypted = cryptoService.decrypt(encryptedData, key);
        String jsonData = new String(decrypted, StandardCharsets.UTF_8);

        this.currentVault = objectMapper.readValue(jsonData, Vault.class);
        this.currentKey = key;
        this.currentVaultPath = vaultPath;

        SecureMemory.clear(decrypted);
    }

    /**
     * Saves the current vault to disk.
     * 
     * @throws Exception if saving fails
     */
    public void saveVault() throws Exception {
        if (currentVault == null || currentKey == null || currentVaultPath == null) {
            throw new IllegalStateException("No vault is currently open");
        }

        // Read existing file to get salt and iterations
        String content = Files.readString(currentVaultPath, StandardCharsets.UTF_8);
        Map<String, Object> vaultFile = objectMapper.readValue(content, Map.class);

        byte[] salt = Base64.getDecoder().decode((String) vaultFile.get("salt"));
        int iterations = (int) vaultFile.get("iterations");

        saveVault(salt, iterations);
    }

    /**
     * Saves the vault with the specified salt and iterations.
     */
    private void saveVault(byte[] salt, int iterations) throws Exception {
        String jsonData = objectMapper.writeValueAsString(currentVault);
        byte[] plaintext = jsonData.getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = cryptoService.encrypt(plaintext, currentKey);

        Map<String, Object> vaultFile = new HashMap<>();
        vaultFile.put("version", Constants.VAULT_VERSION);
        vaultFile.put("salt", Base64.getEncoder().encodeToString(salt));
        vaultFile.put("iterations", iterations);
        vaultFile.put("encryptedData", Base64.getEncoder().encodeToString(encrypted));

        String fileContent = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(vaultFile);

        Files.writeString(currentVaultPath, fileContent, StandardCharsets.UTF_8);

        SecureMemory.clear(plaintext);
    }

    /**
     * Locks the vault by clearing the key and vault from memory.
     */
    public void lockVault() {
        this.currentVault = null;
        this.currentKey = null;
        // Path is kept so we know which vault to prompt for password
    }

    /**
     * Checks if a vault is currently unlocked.
     * 
     * @return true if a vault is unlocked
     */
    public boolean isUnlocked() {
        return currentVault != null && currentKey != null;
    }

    /**
     * Gets the currently open vault.
     * 
     * @return The current vault, or null if no vault is open
     */
    public Vault getCurrentVault() {
        return currentVault;
    }

    /**
     * Gets the path of the currently open vault.
     * 
     * @return The vault path, or null if no vault is open
     */
    public Path getCurrentVaultPath() {
        return currentVaultPath;
    }

    /**
     * Checks if a vault file exists at the default location.
     * 
     * @return true if default vault exists
     */
    public boolean defaultVaultExists() {
        return Files.exists(Constants.DEFAULT_VAULT_PATH);
    }

    /**
     * Gets the default vault path.
     * 
     * @return The default vault path
     */
    public Path getDefaultVaultPath() {
        return Constants.DEFAULT_VAULT_PATH;
    }

    /**
     * Changes the master password. Verifies the current password, then re-encrypts
     * the vault with a key derived from the new password (new salt).
     *
     * @param currentPassword The current master password (for verification)
     * @param newPassword     The new master password
     * @throws IllegalArgumentException if current password is wrong, new equals current, or new too short
     * @throws Exception                 if file or crypto operations fail
     */
    public void changeMasterPassword(char[] currentPassword, char[] newPassword) throws Exception {
        if (currentVault == null || currentKey == null || currentVaultPath == null) {
            throw new IllegalStateException("No vault is currently open");
        }
        if (newPassword == null || newPassword.length < Constants.MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("New password must be at least " + Constants.MIN_PASSWORD_LENGTH + " characters");
        }
        if (Arrays.equals(currentPassword, newPassword)) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        String content = Files.readString(currentVaultPath, StandardCharsets.UTF_8);
        Map<String, Object> vaultFile = objectMapper.readValue(content, Map.class);

        byte[] salt = Base64.getDecoder().decode((String) vaultFile.get("salt"));
        int iterations = (int) vaultFile.get("iterations");
        byte[] encryptedData = Base64.getDecoder().decode((String) vaultFile.get("encryptedData"));

        SecretKey keyFromCurrent = keyDerivation.deriveKey(currentPassword, salt, iterations);
        byte[] decrypted;
        try {
            decrypted = cryptoService.decrypt(encryptedData, keyFromCurrent);
        } catch (Exception e) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        byte[] newSalt = cryptoService.generateSalt(Constants.SALT_LENGTH);
        SecretKey newKey = keyDerivation.deriveKey(newPassword, newSalt);

        byte[] plaintext = decrypted;
        byte[] reEncrypted = cryptoService.encrypt(plaintext, newKey);

        Map<String, Object> newVaultFile = new HashMap<>();
        newVaultFile.put("version", Constants.VAULT_VERSION);
        newVaultFile.put("salt", Base64.getEncoder().encodeToString(newSalt));
        newVaultFile.put("iterations", keyDerivation.getDefaultIterations());
        newVaultFile.put("encryptedData", Base64.getEncoder().encodeToString(reEncrypted));

        String fileContent = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(newVaultFile);

        Files.writeString(currentVaultPath, fileContent, StandardCharsets.UTF_8);

        this.currentKey = newKey;

        SecureMemory.clear(decrypted);
    }

    /**
     * Exports (copies) the current vault file to the given path.
     *
     * @param targetPath Where to save the vault file (e.g. user-chosen path)
     * @throws Exception if no vault is open or copy fails
     */
    public void exportVault(Path targetPath) throws Exception {
        if (currentVaultPath == null || !Files.exists(currentVaultPath)) {
            throw new IllegalStateException("No vault is currently open");
        }
        Files.copy(currentVaultPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
