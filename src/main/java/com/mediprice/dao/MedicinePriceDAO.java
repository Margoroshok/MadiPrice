package com.mediprice.dao;

import com.mediprice.model.MedicinePrice;
import com.mediprice.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for MedicinePrice (availability + price at a pharmacy).
 */
public class MedicinePriceDAO {

    /**
     * Returns all pharmacy prices for a given medicine, sorted by price ascending.
     */
    public List<MedicinePrice> findByMedicineId(int medicineId) throws SQLException {
        List<MedicinePrice> list = new ArrayList<>();
        String sql = """
            SELECT mp.*, m.name AS medicine_name,
                   p.name AS pharmacy_name, p.address AS pharmacy_address, p.city AS pharmacy_city
            FROM medicine_prices mp
            JOIN medicines m ON mp.medicine_id = m.id
            JOIN pharmacies p ON mp.pharmacy_id = p.id
            WHERE mp.medicine_id = ?
            ORDER BY mp.price ASC
            """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<MedicinePrice> findByPharmacyId(int pharmacyId) throws SQLException {
        List<MedicinePrice> list = new ArrayList<>();
        String sql = """
            SELECT mp.*, m.name AS medicine_name,
                   p.name AS pharmacy_name, p.address AS pharmacy_address, p.city AS pharmacy_city
            FROM medicine_prices mp
            JOIN medicines m ON mp.medicine_id = m.id
            JOIN pharmacies p ON mp.pharmacy_id = p.id
            WHERE mp.pharmacy_id = ?
            ORDER BY m.name ASC
            """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, pharmacyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<MedicinePrice> findByMedicineAndPharmacy(int medicineId, int pharmacyId) throws SQLException {
        String sql = """
            SELECT mp.*, m.name AS medicine_name,
                   p.name AS pharmacy_name, p.address AS pharmacy_address, p.city AS pharmacy_city
            FROM medicine_prices mp
            JOIN medicines m ON mp.medicine_id = m.id
            JOIN pharmacies p ON mp.pharmacy_id = p.id
            WHERE mp.medicine_id = ? AND mp.pharmacy_id = ?
            """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setInt(2, pharmacyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public int insert(MedicinePrice mp) throws SQLException {
        String sql = "INSERT INTO medicine_prices (medicine_id, pharmacy_id, price, quantity) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, mp.getMedicineId());
            ps.setInt(2, mp.getPharmacyId());
            ps.setDouble(3, mp.getPrice());
            ps.setInt(4, mp.getQuantity());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public void update(MedicinePrice mp) throws SQLException {
        String sql = "UPDATE medicine_prices SET price=?, quantity=?, updated_at=datetime('now') WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, mp.getPrice());
            ps.setInt(2, mp.getQuantity());
            ps.setInt(3, mp.getId());
            ps.executeUpdate();
        }
    }

    public void updateQuantity(int medicineId, int pharmacyId, int newQuantity) throws SQLException {
        String sql = "UPDATE medicine_prices SET quantity=?, updated_at=datetime('now') " +
                     "WHERE medicine_id=? AND pharmacy_id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, medicineId);
            ps.setInt(3, pharmacyId);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM medicine_prices WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void upsert(MedicinePrice mp) throws SQLException {
        String sql = """
            INSERT INTO medicine_prices (medicine_id, pharmacy_id, price, quantity)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(medicine_id, pharmacy_id) DO UPDATE SET
              price = excluded.price,
              quantity = excluded.quantity,
              updated_at = datetime('now')
            """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, mp.getMedicineId());
            ps.setInt(2, mp.getPharmacyId());
            ps.setDouble(3, mp.getPrice());
            ps.setInt(4, mp.getQuantity());
            ps.executeUpdate();
        }
    }

    private MedicinePrice mapRow(ResultSet rs) throws SQLException {
        MedicinePrice mp = new MedicinePrice();
        mp.setId(rs.getInt("id"));
        mp.setMedicineId(rs.getInt("medicine_id"));
        mp.setPharmacyId(rs.getInt("pharmacy_id"));
        mp.setPrice(rs.getDouble("price"));
        mp.setQuantity(rs.getInt("quantity"));
        try {
            mp.setMedicineName(rs.getString("medicine_name"));
            mp.setPharmacyName(rs.getString("pharmacy_name"));
            mp.setPharmacyAddress(rs.getString("pharmacy_address"));
            mp.setPharmacyCity(rs.getString("pharmacy_city"));
        } catch (SQLException ignored) {}
        String updatedAt = rs.getString("updated_at");
        if (updatedAt != null) {
            try { mp.setUpdatedAt(LocalDateTime.parse(updatedAt.replace(" ", "T"))); }
            catch (Exception ignored) {}
        }
        return mp;
    }
}
