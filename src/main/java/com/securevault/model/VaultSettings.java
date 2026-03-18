package com.securevault.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * User preferences and settings stored within the vault.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VaultSettings {

    private int autoLockTimeoutMinutes = 5;
    private int clipboardClearSeconds = 30;
    private boolean showPasswordsByDefault = false;
    private int defaultPasswordLength = 16;
    private boolean includeUppercase = true;
    private boolean includeLowercase = true;
    private boolean includeNumbers = true;
    private boolean includeSymbols = true;

    public VaultSettings() {
        // Default constructor with default values
    }

    // Getters and Setters

    public int getAutoLockTimeoutMinutes() {
        return autoLockTimeoutMinutes;
    }

    public void setAutoLockTimeoutMinutes(int autoLockTimeoutMinutes) {
        this.autoLockTimeoutMinutes = autoLockTimeoutMinutes;
    }

    public int getClipboardClearSeconds() {
        return clipboardClearSeconds;
    }

    public void setClipboardClearSeconds(int clipboardClearSeconds) {
        this.clipboardClearSeconds = clipboardClearSeconds;
    }

    public boolean isShowPasswordsByDefault() {
        return showPasswordsByDefault;
    }

    public void setShowPasswordsByDefault(boolean showPasswordsByDefault) {
        this.showPasswordsByDefault = showPasswordsByDefault;
    }

    public int getDefaultPasswordLength() {
        return defaultPasswordLength;
    }

    public void setDefaultPasswordLength(int defaultPasswordLength) {
        this.defaultPasswordLength = defaultPasswordLength;
    }

    public boolean isIncludeUppercase() {
        return includeUppercase;
    }

    public void setIncludeUppercase(boolean includeUppercase) {
        this.includeUppercase = includeUppercase;
    }

    public boolean isIncludeLowercase() {
        return includeLowercase;
    }

    public void setIncludeLowercase(boolean includeLowercase) {
        this.includeLowercase = includeLowercase;
    }

    public boolean isIncludeNumbers() {
        return includeNumbers;
    }

    public void setIncludeNumbers(boolean includeNumbers) {
        this.includeNumbers = includeNumbers;
    }

    public boolean isIncludeSymbols() {
        return includeSymbols;
    }

    public void setIncludeSymbols(boolean includeSymbols) {
        this.includeSymbols = includeSymbols;
    }
}
