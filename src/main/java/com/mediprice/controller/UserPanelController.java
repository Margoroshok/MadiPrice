package com.mediprice.controller;

import com.mediprice.model.Reservation;
import com.mediprice.model.Pharmacy;
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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the User panel (reservation history + nearest pharmacy).
 */
public class UserPanelController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    // Reservations tab
    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> resMedicineCol;
    @FXML private TableColumn<Reservation, String> resPharmacyCol;
    @FXML private TableColumn<Reservation, Integer> resQtyCol;
    @FXML private TableColumn<Reservation, String> resDateCol;
    @FXML private TableColumn<Reservation, String> resStatusCol;
    @FXML private Button cancelReservationButton;

    // Nearest pharmacy tab
    @FXML private TextField latField;
    @FXML private TextField lonField;
    @FXML private TableView<Pharmacy> pharmacyTable;
    @FXML private TableColumn<Pharmacy, String> pharmNameCol;
    @FXML private TableColumn<Pharmacy, String> pharmCityCol;
    @FXML private TableColumn<Pharmacy, String> pharmAddressCol;
    @FXML private TableColumn<Pharmacy, String> pharmDistCol;

    private final ReservationService reservationService = new ReservationService();
    private final PharmacyService pharmacyService = new PharmacyService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String username = SessionManager.getInstance().getCurrentUser().getUsername();
        welcomeLabel.setText("Welcome, " + username + "!");

        setupReservationsTable();
        setupPharmacyTable();
        loadReservations();
    }

    private void setupReservationsTable() {
        resMedicineCol.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        resPharmacyCol.setCellValueFactory(new PropertyValueFactory<>("pharmacyName"));
        resQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        resDateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        resStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Color-code status
        resStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "PENDING"   -> setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
                    case "CONFIRMED" -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    case "CANCELLED" -> setStyle("-fx-text-fill: #B71C1C;");
                    case "COMPLETED" -> setStyle("-fx-text-fill: #37474F;");
                    default          -> setStyle("");
                }
            }
        });
    }

    private void setupPharmacyTable() {
        pharmNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        pharmCityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        pharmAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        pharmDistCol.setCellValueFactory(cellData -> {
            double dist = cellData.getValue().getDistanceKm();
            if (dist < 0) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(String.format("%.1f km", dist));
        });
    }

    private void loadReservations() {
        try {
            int userId = SessionManager.getInstance().getCurrentUser().getId();
            List<Reservation> list = reservationService.getUserReservations(userId);
            reservationsTable.setItems(FXCollections.observableArrayList(list));
            statusLabel.setText("Your reservations: " + list.size());
        } catch (ServiceException e) {
            statusLabel.setText("Error loading reservations: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelReservation() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { statusLabel.setText("Select a reservation to cancel."); return; }
        if (!selected.isCancellable()) { statusLabel.setText("This reservation cannot be cancelled."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancel reservation for " + selected.getMedicineName() + "?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                int userId = SessionManager.getInstance().getCurrentUser().getId();
                reservationService.cancelReservation(selected.getId(), userId, false);
                statusLabel.setText("Reservation cancelled.");
                loadReservations();
            } catch (ServiceException e) {
                statusLabel.setText("Error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleFindNearby() {
        try {
            double lat = Double.parseDouble(latField.getText().trim());
            double lon = Double.parseDouble(lonField.getText().trim());
            List<Pharmacy> sorted = pharmacyService.findNearestPharmacies(lat, lon);
            pharmacyTable.setItems(FXCollections.observableArrayList(sorted));
            statusLabel.setText("Showing " + sorted.size() + " pharmacies sorted by distance.");
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter valid decimal coordinates (e.g. 52.2297, 21.0122).");
        } catch (ServiceException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearchMedicines() {
        try {
            SceneManager.switchScene("/fxml/Search.fxml");
        } catch (IOException e) {
            statusLabel.setText("Navigation error.");
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            SceneManager.switchScene("/fxml/Login.fxml");
        } catch (IOException e) {
            statusLabel.setText("Navigation error.");
        }
    }
}
