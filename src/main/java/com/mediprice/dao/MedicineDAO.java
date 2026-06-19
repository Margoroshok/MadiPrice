package com.mediprice.dao;

import com.mediprice.model.Medicine;
import com.mediprice.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Medicine entities.
 */
public class MedicineDAO {

    private static final Logger LOGGER = Logger.getLogger(MedicineDAO.class.getName());

    public List<Medicine> findAll() throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines ORDER BY name";
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Medicine> findById(int id) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Searches medicines by name using partial (LIKE) matching.
     */
    public List<Medicine> searchByName(String query) throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE LOWER(name) LIKE ? ORDER BY name";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + query.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int insert(Medicine medicine) throws SQLException {
        String sql = "INSERT INTO medicines (name, manufacturer, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, medicine.getName());
            ps.setString(2, medicine.getManufacturer());
            ps.setString(3, medicine.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public void update(Medicine medicine) throws SQLException {
        String sql = "UPDATE medicines SET name=?, manufacturer=?, description=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, medicine.getName());
            ps.setString(2, medicine.getManufacturer());
            ps.setString(3, medicine.getDescription());
            ps.setInt(4, medicine.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM medicines WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        m.setManufacturer(rs.getString("manufacturer"));
        m.setDescription(rs.getString("description"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            try { m.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T"))); }
            catch (Exception ignored) {}
        }
        return m;
    }
}
