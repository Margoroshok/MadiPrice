package com.mediprice.controller;

import com.mediprice.model.Medicine;
import com.mediprice.model.Pharmacy;
import com.mediprice.model.Reservation;
import com.mediprice.service.MedicineService;
import com.mediprice.service.PharmacyService;
import com.mediprice.service.ReservationService;
import com.mediprice.service.ServiceException;
import com.mediprice.util.SceneManager;
import com.mediprice.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Administrator panel.
 * Provides full CRUD for medicines, pharmacies, and reservation management.
 */
public class AdminPanelController implements Initializable {

    @FXML private Label statusLabel;

    // --- Medicines Tab ---
    @FXML private TableView<Medicine> medicinesTable;
    @FXML private TableColumn<Medicine, Integer> medIdCol;
    @FXML private TableColumn<Medicine, String> medNameCol;
    @FXML private TableColumn<Medicine, String> medMfgCol;
    @FXML private TableColumn<Medicine, String> medDescCol;

    // --- Pharmacies Tab ---
    @FXML private TableView<Pharmacy> pharmaciesTable;
    @FXML private TableColumn<Pharmacy, Integer> pharmIdCol;
    @FXML private TableColumn<Pharmacy, String> pharmNameCol;
    @FXML private TableColumn<Pharmacy, String> pharmCityCol;
    @FXML private TableColumn<Pharmacy, String> pharmAddressCol;
    @FXML private TableColumn<Pharmacy, String> pharmLatCol;
    @FXML private TableColumn<Pharmacy, String> pharmLonCol;

    // --- Reservations Tab ---
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, Integer> resIdCol;
    @FXML private TableColumn<Reservation, String> resUserCol;
    @FXML private TableColumn<Reservation, String> resMedCol;
    @FXML private TableColumn<Reservation, String> resPharmCol;
    @FXML private TableColumn<Reservation, Integer> resQtyCol;
    @FXML private TableColumn<Reservation, String> resDateCol;
    @FXML private TableColumn<Reservation, String> resStatusCol;

    private final MedicineService medicineService = new MedicineService();
    private final PharmacyService pharmacyService = new PharmacyService();
    private final ReservationService reservationService = new ReservationService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMedicinesTable();
        setupPharmaciesTable();
        setupReservationsTable();
        loadAll();
    }

    // ===== SETUP =====

    private void setupMedicinesTable() {
        medIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        medNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        medMfgCol.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        medDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void setupPharmaciesTable() {
        pharmIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        pharmNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        pharmCityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        pharmAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        pharmLatCol.setCellValueFactory(cd ->
                new SimpleStringProperty(String.format("%.4f", cd.getValue().getLatitude())));
        pharmLonCol.setCellValueFactory(cd ->
                new SimpleStringProperty(String.format("%.4f", cd.getValue().getLongitude())));
    }

    private void setupReservationsTable() {
        resIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        resUserCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        resMedCol.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        resPharmCol.setCellValueFactory(new PropertyValueFactory<>("pharmacyName"));
        resQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        resDateCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getFormattedDate()));
        resStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadAll() {
        loadMedicines();
        loadPharmacies();
        loadReservations();
    }

    private void loadMedicines() {
        try {
            List<Medicine> list = medicineService.getAllMedicines();
            medicinesTable.setItems(FXCollections.observableArrayList(list));
        } catch (ServiceException e) { setStatus("Error loading medicines: " + e.getMessage()); }
    }

    private void loadPharmacies() {
        try {
            List<Pharmacy> list = pharmacyService.getAllPharmacies();
            pharmaciesTable.setItems(FXCollections.observableArrayList(list));
        } catch (ServiceException e) { setStatus("Error loading pharmacies: " + e.getMessage()); }
    }

    private void loadReservations() {
        try {
            List<Reservation> list = reservationService.getAllReservations();
            reservationsTable.setItems(FXCollections.observableArrayList(list));
        } catch (ServiceException e) { setStatus("Error loading reservations: " + e.getMessage()); }
    }

    // ===== MEDICINE CRUD =====

    @FXML
    private void handleAddMedicine() {
        Dialog<Medicine> dialog = buildMedicineDialog(null);
        dialog.showAndWait().ifPresent(m -> {
            try {
                medicineService.createMedicine(m.getName(), m.getManufacturer(), m.getDescription());
                setStatus("Medicine added: " + m.getName());
                loadMedicines();
            } catch (ServiceException e) { showError(e.getMessage()); }
        });
    }

    @FXML
    private void handleEditMedicine() {
        Medicine selected = medicinesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a medicine to edit."); return; }

        Dialog<Medicine> dialog = buildMedicineDialog(selected);
        dialog.showAndWait().ifPresent(m -> {
            try {
                selected.setName(m.getName());
                selected.setManufacturer(m.getManufacturer());
                selected.setDescription(m.getDescription());
                medicineService.updateMedicine(selected);
                setStatus("Medicine updated.");
                loadMedicines();
            } catch (ServiceException e) { showError(e.getMessage()); }
        });
    }

    @FXML
    private void handleDeleteMedicine() {
        Medicine selected = medicinesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a medicine to delete."); return; }

        if (confirmDelete("medicine '" + selected.getName() + "'")) {
            try {
                medicineService.deleteMedicine(selected.getId());
                setStatus("Medicine deleted.");
                loadMedicines();
            } catch (ServiceException e) { showError(e.getMessage()); }
        }
    }

    private Dialog<Medicine> buildMedicineDialog(Medicine existing) {
        Dialog<Medicine> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Medicine" : "Edit Medicine");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        TextField mfgField = new TextField(existing != null ? existing.getManufacturer() : "");
        TextArea descArea = new TextArea(existing != null ? existing.getDescription() : "");
        descArea.setPrefRowCount(3);

        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Manufacturer:"), mfgField);
        grid.addRow(2, new Label("Description:"), descArea);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Medicine m = new Medicine();
                m.setName(nameField.getText().trim());
                m.setManufacturer(mfgField.getText().trim());
                m.setDescription(descArea.getText().trim());
                return m;
            }
            return null;
        });
        return dialog;
    }

    // ===== PHARMACY CRUD =====

    @FXML
    private void handleAddPharmacy() {
        Dialog<Pharmacy> dialog = buildPharmacyDialog(null);
        dialog.showAndWait().ifPresent(p -> {
            try {
                pharmacyService.createPharmacy(p.getName(), p.getAddress(), p.getCity(),
                        p.getLatitude(), p.getLongitude(), p.getPhone());
                setStatus("Pharmacy added: " + p.getName());
                loadPharmacies();
            } catch (ServiceException e) { showError(e.getMessage()); }
        });
    }

    @FXML
    private void handleEditPharmacy() {
        Pharmacy selected = pharmaciesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a pharmacy to edit."); return; }

        Dialog<Pharmacy> dialog = buildPharmacyDialog(selected);
        dialog.showAndWait().ifPresent(p -> {
            try {
                selected.setName(p.getName());
                selected.setAddress(p.getAddress());
                selected.setCity(p.getCity());
                selected.setLatitude(p.getLatitude());
                selected.setLongitude(p.getLongitude());
                selected.setPhone(p.getPhone());
                pharmacyService.updatePharmacy(selected);
                setStatus("Pharmacy updated.");
                loadPharmacies();
            } catch (ServiceException e) { showError(e.getMessage()); }
        });
    }

    @FXML
    private void handleDeletePharmacy() {
        Pharmacy selected = pharmaciesTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a pharmacy to delete."); return; }

        if (confirmDelete("pharmacy '" + selected.getName() + "'")) {
            try {
                pharmacyService.deletePharmacy(selected.getId());
                setStatus("Pharmacy deleted.");
                loadPharmacies();
            } catch (ServiceException e) { showError(e.getMessage()); }
        }
    }

    private Dialog<Pharmacy> buildPharmacyDialog(Pharmacy existing) {
        Dialog<Pharmacy> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Pharmacy" : "Edit Pharmacy");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        TextField addressField = new TextField(existing != null ? existing.getAddress() : "");
        TextField cityField = new TextField(existing != null ? existing.getCity() : "");
        TextField latField = new TextField(existing != null ? String.valueOf(existing.getLatitude()) : "");
        TextField lonField = new TextField(existing != null ? String.valueOf(existing.getLongitude()) : "");
        TextField phoneField = new TextField(existing != null ? existing.getPhone() : "");

        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Address:"), addressField);
        grid.addRow(2, new Label("City:"), cityField);
        grid.addRow(3, new Label("Latitude:"), latField);
        grid.addRow(4, new Label("Longitude:"), lonField);
        grid.addRow(5, new Label("Phone:"), phoneField);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Pharmacy p = new Pharmacy();
                    p.setName(nameField.getText().trim());
                    p.setAddress(addressField.getText().trim());
                    p.setCity(cityField.getText().trim());
                    p.setLatitude(Double.parseDouble(latField.getText().trim()));
                    p.setLongitude(Double.parseDouble(lonField.getText().trim()));
                    p.setPhone(phoneField.getText().trim());
                    return p;
                } catch (NumberFormatException e) {
                    showError("Invalid latitude or longitude.");
                    return null;
                }
            }
            return null;
        });
        return dialog;
    }

    // ===== RESERVATIONS =====

    @FXML
    private void handleCancelReservation() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a reservation."); return; }
        if (!selected.isCancellable()) { setStatus("Cannot cancel reservation with status: " + selected.getStatus()); return; }

        try {
            reservationService.cancelReservation(selected.getId(),
                    SessionManager.getInstance().getCurrentUser().getId(), true);
            setStatus("Reservation cancelled.");
            loadReservations();
        } catch (ServiceException e) { showError(e.getMessage()); }
    }

    @FXML
    private void handleConfirmReservation() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a reservation."); return; }
        try {
            reservationService.updateReservationStatus(selected.getId(), "CONFIRMED");
            setStatus("Reservation confirmed.");
            loadReservations();
        } catch (ServiceException e) { showError(e.getMessage()); }
    }

    @FXML
    private void handleCompleteReservation() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { setStatus("Select a reservation."); return; }
        try {
            reservationService.updateReservationStatus(selected.getId(), "COMPLETED");
            setStatus("Reservation marked as completed.");
            loadReservations();
        } catch (ServiceException e) { showError(e.getMessage()); }
    }

    // ===== NAVIGATION =====

    @FXML
    private void handleSearchMedicines() {
        try {
            SceneManager.switchScene("/fxml/Search.fxml");
        } catch (IOException e) { setStatus("Navigation error."); }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            SceneManager.switchScene("/fxml/Login.fxml");
        } catch (IOException e) { setStatus("Navigation error."); }
    }

    @FXML
    private void handleRefresh() {
        loadAll();
        setStatus("Data refreshed.");
    }

    // ===== HELPERS =====

    private boolean confirmDelete(String target) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + target + "?\nThis cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Delete");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }
}
