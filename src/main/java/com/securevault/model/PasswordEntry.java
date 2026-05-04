package com.securevault.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single password entry in the vault.
 * Contains all the fields for storing credentials including
 * title, username, password, URL, notes, and metadata.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PasswordEntry {

    private String id;
    private String title;
    private String username;
    private String password;
    private String url;
    private String notes;
    private String categoryId;
    private List<String> passwordHistory;
    private Instant createdAt;
    private Instant modifiedAt;

    private static final int PASSWORD_HISTORY_LIMIT = 5;

    public PasswordEntry() {
        this.id = UUID.randomUUID().toString();
        this.passwordHistory = new ArrayList<>();
        this.createdAt = Instant.now();
        this.modifiedAt = Instant.now();
    }

    @SuppressWarnings("unused")
    public PasswordEntry(String title, String username, String password) {
        this();
        this.title = title;
        this.username = username;
        this.password = password;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.modifiedAt = Instant.now();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.modifiedAt = Instant.now();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (this.passwordHistory == null) {
            this.passwordHistory = new ArrayList<>();
        }

        if (this.password != null && !this.password.isEmpty() && !this.password.equals(password)) {
            this.passwordHistory.add(this.password);
            trimPasswordHistory();
        }
        this.password = password;
        this.modifiedAt = Instant.now();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        this.modifiedAt = Instant.now();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.modifiedAt = Instant.now();
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        this.modifiedAt = Instant.now();
    }

    public List<String> getPasswordHistory() {
        if (passwordHistory == null) {
            passwordHistory = new ArrayList<>();
        }
        return passwordHistory;
    }

    @SuppressWarnings("unused")
    public void setPasswordHistory(List<String> passwordHistory) {
        if (passwordHistory == null) {
            this.passwordHistory = new ArrayList<>();
        } else {
            this.passwordHistory = new ArrayList<>(passwordHistory);
            trimPasswordHistory();
        }
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @SuppressWarnings("unused")
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    @SuppressWarnings("unused")
    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    /**
     * Updates the modified timestamp to the current time.
     */
    @SuppressWarnings("unused")
    public void touch() {
        this.modifiedAt = Instant.now();
    }

    /**
     * Returns true if candidate matches any of the last {@code count} old passwords.
     */
    public boolean usesRecentPassword(String candidate, int count) {
        if (candidate == null || candidate.isEmpty()) {
            return false;
        }

        List<String> history = getPasswordHistory();
        int checks = Math.min(count, history.size());
        for (int i = history.size() - 1; i >= history.size() - checks; i--) {
            if (candidate.equals(history.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void trimPasswordHistory() {
        if (passwordHistory == null) {
            return;
        }
        int overflow = passwordHistory.size() - PASSWORD_HISTORY_LIMIT;
        if (overflow > 0) {
            passwordHistory.subList(0, overflow).clear();
        }
    }

    @Override
    public String toString() {
        return "PasswordEntry{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", username='" + username + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
