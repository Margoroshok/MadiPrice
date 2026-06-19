package com.mediprice.service;

import com.mediprice.dao.MedicinePriceDAO;
import com.mediprice.dao.ReservationDAO;
import com.mediprice.model.MedicinePrice;
import com.mediprice.model.Reservation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReservationService.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationDAO reservationDAO;
    @Mock private MedicinePriceDAO medicinePriceDAO;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("createReservation with sufficient stock should succeed")
    void createReservation_sufficientStock_succeeds() throws Exception {
        MedicinePrice mp = buildMedicinePrice(1, 1, 5.99, 20);
        when(medicinePriceDAO.findByMedicineAndPharmacy(1, 1)).thenReturn(Optional.of(mp));
        when(reservationDAO.insert(any())).thenReturn(10);

        Reservation r = reservationService.createReservation(1, 1, 1, 3);

        assertNotNull(r);
        assertEquals(3, r.getQuantity());
        assertEquals("PENDING", r.getStatus());
        verify(medicinePriceDAO).updateQuantity(1, 1, 17); // 20 - 3 = 17
    }

    @Test
    @DisplayName("createReservation with insufficient stock should throw")
    void createReservation_insufficientStock_throwsException() throws Exception {
        MedicinePrice mp = buildMedicinePrice(1, 1, 5.99, 2);
        when(medicinePriceDAO.findByMedicineAndPharmacy(1, 1)).thenReturn(Optional.of(mp));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> reservationService.createReservation(1, 1, 1, 5));
        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    @DisplayName("createReservation for unavailable medicine should throw")
    void createReservation_notAvailableAtPharmacy_throwsException() throws Exception {
        when(medicinePriceDAO.findByMedicineAndPharmacy(1, 1)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> reservationService.createReservation(1, 1, 1, 1));
    }

    @Test
    @DisplayName("createReservation with zero quantity should throw")
    void createReservation_zeroQuantity_throwsException() {
        assertThrows(ServiceException.class,
                () -> reservationService.createReservation(1, 1, 1, 0));
    }

    @Test
    @DisplayName("createReservation with quantity over 100 should throw")
    void createReservation_tooManyUnits_throwsException() {
        assertThrows(ServiceException.class,
                () -> reservationService.createReservation(1, 1, 1, 101));
    }

    @Test
    @DisplayName("cancelReservation by owner should succeed and restore stock")
    void cancelReservation_byOwner_succeedsAndRestoresStock() throws Exception {
        Reservation res = buildReservation(1, 1, 1, 1, 3, "PENDING");
        MedicinePrice mp = buildMedicinePrice(1, 1, 5.99, 10);
        when(reservationDAO.findById(1)).thenReturn(Optional.of(res));
        when(medicinePriceDAO.findByMedicineAndPharmacy(1, 1)).thenReturn(Optional.of(mp));

        reservationService.cancelReservation(1, 1, false);

        verify(reservationDAO).updateStatus(1, "CANCELLED");
        verify(medicinePriceDAO).updateQuantity(1, 1, 13); // 10 + 3
    }

    @Test
    @DisplayName("cancelReservation by different user should throw")
    void cancelReservation_byDifferentUser_throwsException() throws Exception {
        Reservation res = buildReservation(1, 2, 1, 1, 1, "PENDING"); // userId=2
        when(reservationDAO.findById(1)).thenReturn(Optional.of(res));

        assertThrows(ServiceException.class,
                () -> reservationService.cancelReservation(1, 99, false)); // user 99 tries
    }

    @Test
    @DisplayName("cancelReservation by admin should always succeed")
    void cancelReservation_byAdmin_succeeds() throws Exception {
        Reservation res = buildReservation(1, 2, 1, 1, 1, "PENDING");
        MedicinePrice mp = buildMedicinePrice(1, 1, 5.99, 5);
        when(reservationDAO.findById(1)).thenReturn(Optional.of(res));
        when(medicinePriceDAO.findByMedicineAndPharmacy(1, 1)).thenReturn(Optional.of(mp));

        reservationService.cancelReservation(1, 999, true); // admin

        verify(reservationDAO).updateStatus(1, "CANCELLED");
    }

    @Test
    @DisplayName("cancelReservation on COMPLETED reservation should throw")
    void cancelReservation_completedReservation_throwsException() throws Exception {
        Reservation res = buildReservation(1, 1, 1, 1, 1, "COMPLETED");
        when(reservationDAO.findById(1)).thenReturn(Optional.of(res));

        assertThrows(ServiceException.class,
                () -> reservationService.cancelReservation(1, 1, false));
    }

    // Helpers
    private MedicinePrice buildMedicinePrice(int medId, int pharmId, double price, int qty) {
        MedicinePrice mp = new MedicinePrice(medId, pharmId, price, qty);
        mp.setId(1);
        return mp;
    }

    private Reservation buildReservation(int id, int userId, int medId, int pharmId,
                                         int qty, String status) {
        Reservation r = new Reservation(userId, medId, pharmId, qty);
        r.setId(id);
        r.setStatus(status);
        return r;
    }
}
