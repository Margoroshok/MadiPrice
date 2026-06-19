package com.mediprice.service;

import com.mediprice.dao.MedicineDAO;
import com.mediprice.dao.MedicinePriceDAO;
import com.mediprice.model.Medicine;
import com.mediprice.model.MedicinePrice;
import com.mediprice.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service for medicine search, price comparison, and CRUD operations.
 */
public class MedicineService {

    private final MedicineDAO medicineDAO;
    private final MedicinePriceDAO medicinePriceDAO;

    public MedicineService() {
        this.medicineDAO = new MedicineDAO();
        this.medicinePriceDAO = new MedicinePriceDAO();
    }

    public MedicineService(MedicineDAO medicineDAO, MedicinePriceDAO medicinePriceDAO) {
        this.medicineDAO = medicineDAO;
        this.medicinePriceDAO = medicinePriceDAO;
    }

    public List<Medicine> getAllMedicines() throws ServiceException {
        try { return medicineDAO.findAll(); }
        catch (SQLException e) { throw new ServiceException("Failed to load medicines.", e); }
    }

    /**
     * Searches medicines by partial name match.
     */
    public List<Medicine> searchMedicines(String query) throws ServiceException {
        if (query == null || query.isBlank()) {
            return getAllMedicines();
        }
        try { return medicineDAO.searchByName(query.trim()); }
        catch (SQLException e) { throw new ServiceException("Search failed.", e); }
    }

    /**
     * Returns all prices for a medicine, sorted ascending (cheapest first).
     */
    public List<MedicinePrice> getPricesForMedicine(int medicineId) throws ServiceException {
        try { return medicinePriceDAO.findByMedicineId(medicineId); }
        catch (SQLException e) { throw new ServiceException("Failed to load prices.", e); }
    }

    public Optional<Medicine> getMedicineById(int id) throws ServiceException {
        try { return medicineDAO.findById(id); }
        catch (SQLException e) { throw new ServiceException("Failed to find medicine.", e); }
    }

    // --- Admin CRUD ---

    public Medicine createMedicine(String name, String manufacturer, String description)
            throws ServiceException {
        validateMedicineFields(name, manufacturer);
        try {
            Medicine m = new Medicine();
            m.setName(name.trim());
            m.setManufacturer(manufacturer.trim());
            m.setDescription(description != null ? description.trim() : "");
            int id = medicineDAO.insert(m);
            m.setId(id);
            return m;
        } catch (SQLException e) {
            throw new ServiceException("Failed to create medicine.", e);
        }
    }

    public void updateMedicine(Medicine medicine) throws ServiceException {
        validateMedicineFields(medicine.getName(), medicine.getManufacturer());
        try { medicineDAO.update(medicine); }
        catch (SQLException e) { throw new ServiceException("Failed to update medicine.", e); }
    }

    public void deleteMedicine(int id) throws ServiceException {
        try { medicineDAO.delete(id); }
        catch (SQLException e) { throw new ServiceException("Failed to delete medicine.", e); }
    }

    public void upsertMedicinePrice(int medicineId, int pharmacyId, double price, int quantity)
            throws ServiceException {
        if (!ValidationUtil.isValidPrice(price)) throw new ServiceException("Invalid price.");
        if (!ValidationUtil.isValidQuantity(quantity)) throw new ServiceException("Invalid quantity.");
        try {
            MedicinePrice mp = new MedicinePrice(medicineId, pharmacyId, price, quantity);
            medicinePriceDAO.upsert(mp);
        } catch (SQLException e) {
            throw new ServiceException("Failed to save price.", e);
        }
    }

    private void validateMedicineFields(String name, String manufacturer) throws ServiceException {
        if (!ValidationUtil.isNotEmpty(name))
            throw new ServiceException("Medicine name is required.");
        if (name.trim().length() > 200)
            throw new ServiceException("Medicine name too long (max 200 characters).");
        if (!ValidationUtil.isNotEmpty(manufacturer))
            throw new ServiceException("Manufacturer is required.");
    }
}
