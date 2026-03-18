package com.securevault.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents the complete vault structure containing all password entries and categories.
 * 
 * This is the decrypted data structure that gets serialized to JSON
 * and then encrypted before being written to disk.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vault {

    private List<PasswordEntry> entries;
    private List<Category> categories;
    private VaultSettings settings;

    public Vault() {
        this.entries = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.settings = new VaultSettings();
        initializeDefaultCategories();
    }

    /**
     * Initializes default categories for a new vault.
     */
    private void initializeDefaultCategories() {
        categories.add(new Category("General", "folder"));
        categories.add(new Category("Social", "users"));
        categories.add(new Category("Finance", "credit-card"));
        categories.add(new Category("Email", "mail"));
        categories.add(new Category("Work", "briefcase"));
    }

    // Entry operations

    public void addEntry(PasswordEntry entry) {
        entries.add(entry);
    }

    public void removeEntry(String entryId) {
        entries.removeIf(e -> e.getId().equals(entryId));
    }

    public Optional<PasswordEntry> findEntryById(String entryId) {
        return entries.stream()
                .filter(e -> e.getId().equals(entryId))
                .findFirst();
    }

    public List<PasswordEntry> findEntriesByCategory(String categoryId) {
        return entries.stream()
                .filter(e -> categoryId.equals(e.getCategoryId()))
                .toList();
    }

    public List<PasswordEntry> searchEntries(String query) {
        String lowerQuery = query.toLowerCase();
        return entries.stream()
                .filter(e -> 
                    (e.getTitle() != null && e.getTitle().toLowerCase().contains(lowerQuery)) ||
                    (e.getUsername() != null && e.getUsername().toLowerCase().contains(lowerQuery)) ||
                    (e.getUrl() != null && e.getUrl().toLowerCase().contains(lowerQuery)) ||
                    (e.getNotes() != null && e.getNotes().toLowerCase().contains(lowerQuery))
                )
                .toList();
    }

    // Category operations

    public void addCategory(Category category) {
        categories.add(category);
    }

    public void removeCategory(String categoryId) {
        categories.removeIf(c -> c.getId().equals(categoryId));
    }

    public Optional<Category> findCategoryById(String categoryId) {
        return categories.stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst();
    }

    // Getters and Setters

    public List<PasswordEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<PasswordEntry> entries) {
        this.entries = entries;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public VaultSettings getSettings() {
        return settings;
    }

    public void setSettings(VaultSettings settings) {
        this.settings = settings;
    }
}
