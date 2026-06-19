package com.mediprice.model;

import java.time.LocalDateTime;

/**
 * Represents a pharmacy location.
 */
public class Pharmacy {
    private int id;
    private String name;
    private String address;
    private String city;
    private double latitude;
    private double longitude;
    private String phone;
    private LocalDateTime createdAt;

    // Transient: computed distance from user location (not stored in DB)
    private double distanceKm = -1;

    public Pharmacy() {}

    public Pharmacy(int id, String name, String address, String city,
                    double latitude, double longitude, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getFullAddress() {
        return address + ", " + city;
    }

    @Override
    public String toString() {
        return name + " - " + getFullAddress();
    }
}
