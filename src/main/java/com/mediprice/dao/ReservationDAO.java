package com.mediprice.dao;

import com.mediprice.model.Reservation;
import com.mediprice.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Reservation entities.
 */
public class ReservationDAO {

    private static final String SELECT_WITH_JOINS = """
            SELECT r.*,
                   u.username,
                   m.name AS medicine_name,
                   p.name AS pharmacy_name,
                   p.address AS pharmacy_address
            FROM reservations r
            JOIN users u ON r.user_id = u.id
            JOIN medicines m ON r.medicine_id = m.id
            JOIN pharmacies p ON r.pharmacy_id = p.id
            """;

    public List<Reservation> findByUserId(int userId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + " WHERE r.user_id = ? ORDER BY r.reservation_date DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Reservation> findAll() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + " ORDER BY r.reservation_date DESC";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Reservation> findById(int id) throws SQLException {
        String sql = SELECT_WITH_JOINS + " WHERE r.id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public int insert(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations (user_id, medicine_id, pharmacy_id, quantity, status, notes) " +
                     "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservation.getUserId());
            ps.setInt(2, reservation.getMedicineId());
            ps.setInt(3, reservation.getPharmacyId());
            ps.setInt(4, reservation.getQuantity());
            ps.setString(5, reservation.getStatus() != null ? reservation.getStatus() : "PENDING");
            ps.setString(6, reservation.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public void updateStatus(int reservationId, String newStatus) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reservations WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setMedicineId(rs.getInt("medicine_id"));
        r.setPharmacyId(rs.getInt("pharmacy_id"));
        r.setQuantity(rs.getInt("quantity"));
        r.setStatus(rs.getString("status"));
        r.setNotes(rs.getString("notes"));
        r.setUsername(rs.getString("username"));
        r.setMedicineName(rs.getString("medicine_name"));
        r.setPharmacyName(rs.getString("pharmacy_name"));
        r.setPharmacyAddress(rs.getString("pharmacy_address"));
        String dateStr = rs.getString("reservation_date");
        if (dateStr != null) {
            try { r.setReservationDate(LocalDateTime.parse(dateStr.replace(" ", "T"))); }
            catch (Exception ignored) {}
        }
        return r;
    }
}
