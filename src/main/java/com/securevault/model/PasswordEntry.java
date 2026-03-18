package com.securevault.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single password entry in the vault.
 * 
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
    private Instant createdAt;
    private Instant modifiedAt;

    public PasswordEntry() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.modifiedAt = Instant.now();
    }

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    /**
     * Updates the modified timestamp to the current time.
     */
    public void touch() {
        this.modifiedAt = Instant.now();
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
