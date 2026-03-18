package com.securevault.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

/**
 * Represents a category or folder for organizing password entries.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {

    private String id;
    private String name;
    private String icon;
    private String parentId;

    public Category() {
        this.id = UUID.randomUUID().toString();
    }

    public Category(String name) {
        this();
        this.name = name;
    }

    public Category(String name, String icon) {
        this(name);
        this.icon = icon;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return name;
    }
}
