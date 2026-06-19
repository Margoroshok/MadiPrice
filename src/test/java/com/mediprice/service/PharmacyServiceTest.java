package com.mediprice.service;

import com.mediprice.dao.PharmacyDAO;
import com.mediprice.model.Pharmacy;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PharmacyService.
 */
@ExtendWith(MockitoExtension.class)
class PharmacyServiceTest {

    @Mock private PharmacyDAO pharmacyDAO;
    @InjectMocks private PharmacyService pharmacyService;

    // Haversine — Warsaw to Krakow should be ~252 km
    @Test
    @DisplayName("Haversine distance Warsaw→Krakow should be approximately 252 km")
    void haversineDistance_warsawToKrakow_approx252km() {
        double dist = PharmacyService.haversineDistance(52.2297, 21.0122, 50.0647, 19.9450);
        assertTrue(dist > 245 && dist < 260,
                "Warsaw→Krakow should be ~252 km, got: " + dist);
    }

    @Test
    @DisplayName("Haversine distance same point should be 0")
    void haversineDistance_samePoint_isZero() {
        double dist = PharmacyService.haversineDistance(52.0, 21.0, 52.0, 21.0);
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    @DisplayName("findNearestPharmacies should sort by distance ascending")
    void findNearestPharmacies_sortedByDistance() throws Exception {
        // Two pharmacies — one close to user (Warsaw), one far (Gdansk)
        Pharmacy close = buildPharmacy(1, "Near Pharmacy", 52.2350, 21.0200); // Warsaw
        Pharmacy far   = buildPharmacy(2, "Far Pharmacy",  54.3520, 18.6466); // Gdansk
        when(pharmacyDAO.findAll()).thenReturn(Arrays.asList(far, close));

        // User is in Warsaw center
        List<Pharmacy> result = pharmacyService.findNearestPharmacies(52.2297, 21.0122);

        assertEquals(2, result.size());
        assertEquals("Near Pharmacy", result.get(0).getName(), "Nearest should be first");
        assertTrue(result.get(0).getDistanceKm() < result.get(1).getDistanceKm());
    }

    @Test
    @DisplayName("findNearestPharmacies with invalid coordinates should throw")
    void findNearestPharmacies_invalidCoords_throwsException() {
        assertThrows(ServiceException.class,
                () -> pharmacyService.findNearestPharmacies(200.0, 21.0)); // lat > 90
        assertThrows(ServiceException.class,
                () -> pharmacyService.findNearestPharmacies(52.0, 200.0)); // lon > 180
    }

    @Test
    @DisplayName("createPharmacy with valid data should succeed")
    void createPharmacy_validData_returnsPharmacy() throws Exception {
        when(pharmacyDAO.insert(any())).thenReturn(5);

        Pharmacy p = pharmacyService.createPharmacy(
                "Test Pharmacy", "Main St 1", "Warsaw", 52.23, 21.01, "+48-22-000");

        assertNotNull(p);
        assertEquals("Test Pharmacy", p.getName());
        assertEquals(5, p.getId());
    }

    @Test
    @DisplayName("createPharmacy with empty name should throw")
    void createPharmacy_emptyName_throwsException() {
        assertThrows(ServiceException.class,
                () -> pharmacyService.createPharmacy("", "Main St", "Warsaw", 52.0, 21.0, ""));
    }

    @Test
    @DisplayName("createPharmacy with invalid coordinates should throw")
    void createPharmacy_invalidCoordinates_throwsException() {
        assertThrows(ServiceException.class,
                () -> pharmacyService.createPharmacy("Pharmacy", "St", "City", 100.0, 21.0, ""));
    }

    @Test
    @DisplayName("deletePharmacy should call DAO")
    void deletePharmacy_callsDAO() throws Exception {
        pharmacyService.deletePharmacy(3);
        verify(pharmacyDAO).delete(3);
    }

    @Test
    @DisplayName("getAllPharmacies SQL error should wrap as ServiceException")
    void getAllPharmacies_sqlError_wrapsAsServiceException() throws Exception {
        when(pharmacyDAO.findAll()).thenThrow(new SQLException("DB down"));
        assertThrows(ServiceException.class, () -> pharmacyService.getAllPharmacies());
    }

    // Helpers
    private Pharmacy buildPharmacy(int id, String name, double lat, double lon) {
        return new Pharmacy(id, name, "Test St", "TestCity", lat, lon, "");
    }
}
