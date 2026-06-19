package com.mediprice.dao;

import com.mediprice.model.Pharmacy;
import com.mediprice.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Pharmacy entities.
 */
public class PharmacyDAO {

    public List<Pharmacy> findAll() throws SQLException {
        List<Pharmacy> list = new ArrayList<>();
        String sql = "SELECT * FROM pharmacies ORDER BY name";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Pharmacy> findById(int id) throws SQLException {
        String sql = "SELECT * FROM pharmacies WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Pharmacy> findByCity(String city) throws SQLException {
        List<Pharmacy> list = new ArrayList<>();
        String sql = "SELECT * FROM pharmacies WHERE LOWER(city) = ? ORDER BY name";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, city.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int insert(Pharmacy pharmacy) throws SQLException {
        String sql = "INSERT INTO pharmacies (name, address, city, latitude, longitude, phone) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pharmacy.getName());
            ps.setString(2, pharmacy.getAddress());
            ps.setString(3, pharmacy.getCity());
            ps.setDouble(4, pharmacy.getLatitude());
            ps.setDouble(5, pharmacy.getLongitude());
            ps.setString(6, pharmacy.getPhone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public void update(Pharmacy pharmacy) throws SQLException {
        String sql = "UPDATE pharmacies SET name=?, address=?, city=?, latitude=?, longitude=?, phone=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, pharmacy.getName());
            ps.setString(2, pharmacy.getAddress());
            ps.setString(3, pharmacy.getCity());
            ps.setDouble(4, pharmacy.getLatitude());
            ps.setDouble(5, pharmacy.getLongitude());
            ps.setString(6, pharmacy.getPhone());
            ps.setInt(7, pharmacy.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM pharmacies WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Pharmacy mapRow(ResultSet rs) throws SQLException {
        Pharmacy p = new Pharmacy();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setAddress(rs.getString("address"));
        p.setCity(rs.getString("city"));
        p.setLatitude(rs.getDouble("latitude"));
        p.setLongitude(rs.getDouble("longitude"));
        p.setPhone(rs.getString("phone"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            try { p.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T"))); }
            catch (Exception ignored) {}
        }
        return p;
    }
}
