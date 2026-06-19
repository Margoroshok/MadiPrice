package com.mediprice.service;

import com.mediprice.dao.MedicinePriceDAO;
import com.mediprice.dao.ReservationDAO;
import com.mediprice.model.MedicinePrice;
import com.mediprice.model.Reservation;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service handling reservation creation, cancellation, and history.
 */
public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final MedicinePriceDAO medicinePriceDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.medicinePriceDAO = new MedicinePriceDAO();
    }

    public ReservationService(ReservationDAO reservationDAO, MedicinePriceDAO medicinePriceDAO) {
        this.reservationDAO = reservationDAO;
        this.medicinePriceDAO = medicinePriceDAO;
    }

    /**
     * Creates a new reservation. Checks stock availability first.
     */
    public Reservation createReservation(int userId, int medicineId, int pharmacyId, int quantity)
            throws ServiceException {
        if (quantity <= 0) throw new ServiceException("Quantity must be at least 1.");
        if (quantity > 100) throw new ServiceException("Cannot reserve more than 100 units at a time.");

        try {
            // Check availability
            Optional<MedicinePrice> priceOpt = medicinePriceDAO.findByMedicineAndPharmacy(medicineId, pharmacyId);
            if (priceOpt.isEmpty()) {
                throw new ServiceException("This medicine is not available at the selected pharmacy.");
            }
            MedicinePrice mp = priceOpt.get();
            if (mp.getQuantity() < quantity) {
                throw new ServiceException("Insufficient stock. Available: " + mp.getQuantity() + " unit(s).");
            }

            // Create the reservation
            Reservation reservation = new Reservation(userId, medicineId, pharmacyId, quantity);
            int id = reservationDAO.insert(reservation);
            reservation.setId(id);

            // Reduce stock
            medicinePriceDAO.updateQuantity(medicineId, pharmacyId, mp.getQuantity() - quantity);

            return reservation;

        } catch (SQLException e) {
            throw new ServiceException("Failed to create reservation. Please try again.", e);
        }
    }

    /**
     * Cancels a reservation (restores stock).
     */
    public void cancelReservation(int reservationId, int requestingUserId, boolean isAdmin)
            throws ServiceException {
        try {
            Optional<Reservation> optRes = reservationDAO.findById(reservationId);
            if (optRes.isEmpty()) {
                throw new ServiceException("Reservation not found.");
            }
            Reservation r = optRes.get();

            // Only the owner or admin can cancel
            if (!isAdmin && r.getUserId() != requestingUserId) {
                throw new ServiceException("You do not have permission to cancel this reservation.");
            }
            if (!r.isCancellable()) {
                throw new ServiceException("Reservation cannot be cancelled (status: " + r.getStatus() + ").");
            }

            reservationDAO.updateStatus(reservationId, "CANCELLED");

            // Restore stock
            Optional<MedicinePrice> priceOpt = medicinePriceDAO
                    .findByMedicineAndPharmacy(r.getMedicineId(), r.getPharmacyId());
            if (priceOpt.isPresent()) {
                MedicinePrice mp = priceOpt.get();
                medicinePriceDAO.updateQuantity(r.getMedicineId(), r.getPharmacyId(),
                        mp.getQuantity() + r.getQuantity());
            }

        } catch (SQLException e) {
            throw new ServiceException("Failed to cancel reservation.", e);
        }
    }

    public List<Reservation> getUserReservations(int userId) throws ServiceException {
        try { return reservationDAO.findByUserId(userId); }
        catch (SQLException e) { throw new ServiceException("Failed to load reservations.", e); }
    }

    public List<Reservation> getAllReservations() throws ServiceException {
        try { return reservationDAO.findAll(); }
        catch (SQLException e) { throw new ServiceException("Failed to load reservations.", e); }
    }

    public void updateReservationStatus(int reservationId, String status) throws ServiceException {
        try { reservationDAO.updateStatus(reservationId, status); }
        catch (SQLException e) { throw new ServiceException("Failed to update status.", e); }
    }
}
