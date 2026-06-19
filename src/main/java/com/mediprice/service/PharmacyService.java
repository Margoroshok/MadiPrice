package com.mediprice.service;

import com.mediprice.dao.PharmacyDAO;
import com.mediprice.model.Pharmacy;
import com.mediprice.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service for pharmacy operations including nearest-pharmacy calculation.
 */
public class PharmacyService {

    private final PharmacyDAO pharmacyDAO;

    public PharmacyService() {
        this.pharmacyDAO = new PharmacyDAO();
    }

    public PharmacyService(PharmacyDAO pharmacyDAO) {
        this.pharmacyDAO = pharmacyDAO;
    }

    public List<Pharmacy> getAllPharmacies() throws ServiceException {
        try { return pharmacyDAO.findAll(); }
        catch (SQLException e) { throw new ServiceException("Failed to load pharmacies.", e); }
    }

    public Optional<Pharmacy> getPharmacyById(int id) throws ServiceException {
        try { return pharmacyDAO.findById(id); }
        catch (SQLException e) { throw new ServiceException("Failed to find pharmacy.", e); }
    }

    /**
     * Returns pharmacies sorted by distance from the given coordinates (Haversine formula).
     */
    public List<Pharmacy> findNearestPharmacies(double userLat, double userLon) throws ServiceException {
        if (!ValidationUtil.isValidCoordinate(userLat, userLon)) {
            throw new ServiceException("Invalid coordinates provided.");
        }
        List<Pharmacy> pharmacies = getAllPharmacies();
        for (Pharmacy p : pharmacies) {
            p.setDistanceKm(haversineDistance(userLat, userLon, p.getLatitude(), p.getLongitude()));
        }
        pharmacies.sort(Comparator.comparingDouble(Pharmacy::getDistanceKm));
        return pharmacies;
    }

    /**
     * Haversine formula: calculates distance in km between two GPS coordinates.
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    // --- Admin CRUD ---

    public Pharmacy createPharmacy(String name, String address, String city,
                                   double lat, double lon, String phone) throws ServiceException {
        validatePharmacyFields(name, address, city, lat, lon);
        try {
            Pharmacy p = new Pharmacy(0, name.trim(), address.trim(), city.trim(), lat, lon,
                    phone != null ? phone.trim() : "");
            int id = pharmacyDAO.insert(p);
            p.setId(id);
            return p;
        } catch (SQLException e) {
            throw new ServiceException("Failed to create pharmacy.", e);
        }
    }

    public void updatePharmacy(Pharmacy pharmacy) throws ServiceException {
        validatePharmacyFields(pharmacy.getName(), pharmacy.getAddress(),
                pharmacy.getCity(), pharmacy.getLatitude(), pharmacy.getLongitude());
        try { pharmacyDAO.update(pharmacy); }
        catch (SQLException e) { throw new ServiceException("Failed to update pharmacy.", e); }
    }

    public void deletePharmacy(int id) throws ServiceException {
        try { pharmacyDAO.delete(id); }
        catch (SQLException e) { throw new ServiceException("Failed to delete pharmacy.", e); }
    }

    private void validatePharmacyFields(String name, String address, String city,
                                        double lat, double lon) throws ServiceException {
        if (!ValidationUtil.isNotEmpty(name)) throw new ServiceException("Pharmacy name is required.");
        if (!ValidationUtil.isNotEmpty(address)) throw new ServiceException("Address is required.");
        if (!ValidationUtil.isNotEmpty(city)) throw new ServiceException("City is required.");
        if (!ValidationUtil.isValidCoordinate(lat, lon))
            throw new ServiceException("Invalid GPS coordinates (lat: -90..90, lon: -180..180).");
    }
}
