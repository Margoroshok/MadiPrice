package com.mediprice.model;

import java.time.LocalDateTime;

/**
 * Represents a medicine/drug in the system.
 */
public class Medicine {
    private int id;
    private String name;
    private String manufacturer;
    private String description;
    private LocalDateTime createdAt;

    public Medicine() {}

    public Medicine(int id, String name, String manufacturer, String description) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + manufacturer + ")";
    }
}
