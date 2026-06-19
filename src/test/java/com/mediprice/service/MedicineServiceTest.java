package com.mediprice.service;

import com.mediprice.dao.MedicineDAO;
import com.mediprice.dao.MedicinePriceDAO;
import com.mediprice.model.Medicine;
import com.mediprice.model.MedicinePrice;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MedicineService.
 */
@ExtendWith(MockitoExtension.class)
class MedicineServiceTest {

    @Mock private MedicineDAO medicineDAO;
    @Mock private MedicinePriceDAO medicinePriceDAO;

    @InjectMocks
    private MedicineService medicineService;

    @Test
    @DisplayName("getAllMedicines should return all medicines")
    void getAllMedicines_returnsAll() throws Exception {
        List<Medicine> expected = Arrays.asList(
                new Medicine(1, "Paracetamol", "PharmaCo", "Pain relief"),
                new Medicine(2, "Ibuprofen", "MedLab", "Anti-inflammatory")
        );
        when(medicineDAO.findAll()).thenReturn(expected);

        List<Medicine> result = medicineService.getAllMedicines();

        assertEquals(2, result.size());
        assertEquals("Paracetamol", result.get(0).getName());
    }

    @Test
    @DisplayName("searchMedicines with empty query should return all")
    void searchMedicines_emptyQuery_returnsAll() throws Exception {
        List<Medicine> all = Arrays.asList(new Medicine(1, "Aspirin", "Bayer", ""));
        when(medicineDAO.findAll()).thenReturn(all);

        List<Medicine> result = medicineService.searchMedicines("");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchMedicines with specific query should filter")
    void searchMedicines_withQuery_callsSearch() throws Exception {
        List<Medicine> filtered = Arrays.asList(new Medicine(1, "Paracetamol 500mg", "PharmaCo", ""));
        when(medicineDAO.searchByName("para")).thenReturn(filtered);

        List<Medicine> result = medicineService.searchMedicines("para");

        assertEquals(1, result.size());
        verify(medicineDAO).searchByName("para");
    }

    @Test
    @DisplayName("getPricesForMedicine should return sorted prices")
    void getPricesForMedicine_returnsPrices() throws Exception {
        List<MedicinePrice> prices = Arrays.asList(
                buildPrice(1, 1, 4.99, 50),
                buildPrice(1, 2, 7.99, 20)
        );
        when(medicinePriceDAO.findByMedicineId(1)).thenReturn(prices);

        List<MedicinePrice> result = medicineService.getPricesForMedicine(1);

        assertEquals(2, result.size());
        assertEquals(4.99, result.get(0).getPrice());
    }

    @Test
    @DisplayName("createMedicine with valid data should succeed")
    void createMedicine_validData_returnsNewMedicine() throws Exception {
        when(medicineDAO.insert(any())).thenReturn(10);

        Medicine m = medicineService.createMedicine("Aspirin 100mg", "Bayer", "Pain relief");

        assertNotNull(m);
        assertEquals("Aspirin 100mg", m.getName());
        assertEquals(10, m.getId());
    }

    @Test
    @DisplayName("createMedicine with empty name should throw ServiceException")
    void createMedicine_emptyName_throwsException() {
        assertThrows(ServiceException.class,
                () -> medicineService.createMedicine("", "Bayer", "Description"));
    }

    @Test
    @DisplayName("createMedicine with empty manufacturer should throw ServiceException")
    void createMedicine_emptyManufacturer_throwsException() {
        assertThrows(ServiceException.class,
                () -> medicineService.createMedicine("Aspirin", "", "Description"));
    }

    @Test
    @DisplayName("deleteMedicine should call DAO delete")
    void deleteMedicine_callsDAO() throws Exception {
        medicineService.deleteMedicine(5);
        verify(medicineDAO).delete(5);
    }

    @Test
    @DisplayName("upsertMedicinePrice with invalid price should throw")
    void upsertMedicinePrice_invalidPrice_throwsException() {
        assertThrows(ServiceException.class,
                () -> medicineService.upsertMedicinePrice(1, 1, -5.0, 10));
    }

    @Test
    @DisplayName("upsertMedicinePrice with invalid quantity should throw")
    void upsertMedicinePrice_invalidQuantity_throwsException() {
        assertThrows(ServiceException.class,
                () -> medicineService.upsertMedicinePrice(1, 1, 5.0, -1));
    }

    @Test
    @DisplayName("DAOException should be wrapped as ServiceException")
    void getAllMedicines_sqlException_wrappedAsServiceException() throws Exception {
        when(medicineDAO.findAll()).thenThrow(new SQLException("DB error"));
        assertThrows(ServiceException.class, () -> medicineService.getAllMedicines());
    }

    // Helpers
    private MedicinePrice buildPrice(int medicineId, int pharmacyId, double price, int qty) {
        MedicinePrice mp = new MedicinePrice(medicineId, pharmacyId, price, qty);
        mp.setPharmacyName("Pharmacy " + pharmacyId);
        return mp;
    }
}
