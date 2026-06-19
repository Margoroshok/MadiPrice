package com.mediprice.controller;

import com.mediprice.model.Medicine;
import com.mediprice.model.MedicinePrice;
import com.mediprice.service.MedicineService;
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
import java.util.ResourceBundle;

/**
 * Controller for medicine search, price comparison, and availability checking.
 */
public class SearchController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button backButton;
    @FXML private Button reserveButton;
    @FXML private Label statusLabel;
    @FXML private Label userLabel;

    // Medicines table
    @FXML private TableView<Medicine> medicinesTable;
    @FXML private TableColumn<Medicine, String> medNameCol;
    @FXML private TableColumn<Medicine, String> medManufacturerCol;
    @FXML private TableColumn<Medicine, String> medDescCol;

    // Prices table
    @FXML private TableView<MedicinePrice> pricesTable;
    @FXML private TableColumn<MedicinePrice, String> pricePharmacyCol;
    @FXML private TableColumn<MedicinePrice, String> priceAddressCol;
    @FXML private TableColumn<MedicinePrice, String> pricePriceCol;
    @FXML private TableColumn<MedicinePrice, String> priceQuantityCol;

    @FXML private Label selectedMedicineLabel;

    private final MedicineService medicineService = new MedicineService();
    private Medicine selectedMedicine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMedicinesTable();
        setupPricesTable();

        // Update UI based on auth state
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();
        reserveButton.setVisible(loggedIn);

        if (loggedIn) {
            userLabel.setText("Logged in as: " + SessionManager.getInstance().getCurrentUser().getUsername());
        } else {
            userLabel.setText("Browsing as Guest");
        }

        searchField.setOnAction(e -> handleSearch());
        loadAllMedicines();
    }

    private void setupMedicinesTable() {
        medNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        medManufacturerCol.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        medDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        medicinesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedMedicine = newVal;
                        selectedMedicineLabel.setText("Selected: " + newVal.getName());
                        loadPricesForMedicine(newVal.getId());
                        reserveButton.setDisable(false);
                    }
                });
    }

    private void setupPricesTable() {
        pricePharmacyCol.setCellValueFactory(new PropertyValueFactory<>("pharmacyName"));
        priceAddressCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPharmacyAddress()
                        + ", " + cellData.getValue().getPharmacyCity()));
        pricePriceCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedPrice()));
        priceQuantityCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAvailabilityText()));

        // Color-code availability
        priceQuantityCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("Out")) setStyle("-fx-text-fill: #e53935;");
                    else if (item.startsWith("Low")) setStyle("-fx-text-fill: #fb8c00;");
                    else setStyle("-fx-text-fill: #43a047;");
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        try {
            List<Medicine> results = medicineService.searchMedicines(query);
            medicinesTable.setItems(FXCollections.observableArrayList(results));
            pricesTable.getItems().clear();
            selectedMedicineLabel.setText("Select a medicine to see prices");
            statusLabel.setText("Found " + results.size() + " medicine(s)");
        } catch (ServiceException e) {
            statusLabel.setText("Search error: " + e.getMessage());
        }
    }

    private void loadAllMedicines() {
        try {
            List<Medicine> all = medicineService.getAllMedicines();
            medicinesTable.setItems(FXCollections.observableArrayList(all));
            statusLabel.setText(all.size() + " medicines available");
        } catch (ServiceException e) {
            statusLabel.setText("Error loading medicines.");
        }
    }

    private void loadPricesForMedicine(int medicineId) {
        try {
            List<MedicinePrice> prices = medicineService.getPricesForMedicine(medicineId);
            pricesTable.setItems(FXCollections.observableArrayList(prices));
            if (prices.isEmpty()) {
                statusLabel.setText("No pricing data for this medicine.");
            } else {
                statusLabel.setText("Found prices at " + prices.size() + " pharmacy(ies). Sorted by price ↑");
            }
        } catch (ServiceException e) {
            statusLabel.setText("Error loading prices.");
        }
    }

    @FXML
    private void handleReserve() {
        if (selectedMedicine == null) {
            statusLabel.setText("Please select a medicine first.");
            return;
        }
        MedicinePrice selectedPrice = pricesTable.getSelectionModel().getSelectedItem();
        if (selectedPrice == null) {
            statusLabel.setText("Please select a pharmacy from the price list.");
            return;
        }

        // Show reservation dialog
        showReservationDialog(selectedMedicine, selectedPrice);
    }

    private void showReservationDialog(Medicine medicine, MedicinePrice price) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Reserve Medicine");
        dialog.setHeaderText("Reserve: " + medicine.getName()
                + "\nPharmacy: " + price.getPharmacyName()
                + "\nPrice: " + price.getFormattedPrice()
                + "\nAvailable: " + price.getQuantity() + " unit(s)");

        ButtonType reserveButtonType = new ButtonType("Reserve", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reserveButtonType, ButtonType.CANCEL);

        Spinner<Integer> spinner = new Spinner<>(1, Math.max(1, price.getQuantity()), 1);
        spinner.setEditable(true);
        dialog.getDialogPane().setContent(new javafx.scene.layout.VBox(
                new Label("Quantity:"), spinner));

        dialog.setResultConverter(btn -> {
            if (btn == reserveButtonType) return spinner.getValue();
            return null;
        });

        dialog.showAndWait().ifPresent(qty -> {
            try {
                com.mediprice.service.ReservationService rs = new com.mediprice.service.ReservationService();
                rs.createReservation(
                        SessionManager.getInstance().getCurrentUser().getId(),
                        medicine.getId(),
                        price.getPharmacyId(),
                        qty
                );
                statusLabel.setText("✓ Reserved " + qty + " x " + medicine.getName()
                        + " at " + price.getPharmacyName());
                loadPricesForMedicine(medicine.getId()); // refresh stock
            } catch (ServiceException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    private void handleBack() {
        try {
            if (SessionManager.getInstance().isLoggedIn()) {
                String role = SessionManager.getInstance().getCurrentUser().getRole();
                SceneManager.switchScene("ADMIN".equals(role) ? "/fxml/AdminPanel.fxml" : "/fxml/UserPanel.fxml");
            } else {
                SceneManager.switchScene("/fxml/Login.fxml");
            }
        } catch (IOException e) {
            statusLabel.setText("Navigation error.");
        }
    }
}
