package com.mediprice.model;

import java.time.LocalDateTime;

/**
 * Represents a medicine's price and availability at a specific pharmacy.
 */
public class MedicinePrice {
    private int id;
    private int medicineId;
    private int pharmacyId;
    private double price;
    private int quantity;
    private LocalDateTime updatedAt;

    // Joined fields for display
    private String medicineName;
    private String pharmacyName;
    private String pharmacyAddress;
    private String pharmacyCity;

    public MedicinePrice() {}

    public MedicinePrice(int medicineId, int pharmacyId, double price, int quantity) {
        this.medicineId = medicineId;
        this.pharmacyId = pharmacyId;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public int getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(int pharmacyId) { this.pharmacyId = pharmacyId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public String getPharmacyAddress() { return pharmacyAddress; }
    public void setPharmacyAddress(String pharmacyAddress) { this.pharmacyAddress = pharmacyAddress; }

    public String getPharmacyCity() { return pharmacyCity; }
    public void setPharmacyCity(String pharmacyCity) { this.pharmacyCity = pharmacyCity; }

    public boolean isAvailable() { return quantity > 0; }

    public String getFormattedPrice() {
        return String.format("%.2f PLN", price);
    }

    public String getAvailabilityText() {
        if (quantity == 0) return "Out of stock";
        if (quantity <= 5) return "Low stock (" + quantity + ")";
        return "In stock (" + quantity + ")";
    }
}
