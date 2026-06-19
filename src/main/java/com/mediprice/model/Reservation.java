package com.mediprice.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a medicine reservation by a user.
 */
public class Reservation {
    public enum Status {
        PENDING, CONFIRMED, CANCELLED, COMPLETED
    }

    private int id;
    private int userId;
    private int medicineId;
    private int pharmacyId;
    private int quantity;
    private LocalDateTime reservationDate;
    private String status;
    private String notes;

    // Joined fields for display
    private String username;
    private String medicineName;
    private String pharmacyName;
    private String pharmacyAddress;

    public Reservation() {}

    public Reservation(int userId, int medicineId, int pharmacyId, int quantity) {
        this.userId = userId;
        this.medicineId = medicineId;
        this.pharmacyId = pharmacyId;
        this.quantity = quantity;
        this.status = Status.PENDING.name();
        this.reservationDate = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public int getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(int pharmacyId) { this.pharmacyId = pharmacyId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDateTime reservationDate) { this.reservationDate = reservationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public String getPharmacyAddress() { return pharmacyAddress; }
    public void setPharmacyAddress(String pharmacyAddress) { this.pharmacyAddress = pharmacyAddress; }

    public boolean isCancellable() {
        return "PENDING".equals(status) || "CONFIRMED".equals(status);
    }

    public String getFormattedDate() {
        if (reservationDate == null) return "";
        return reservationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
