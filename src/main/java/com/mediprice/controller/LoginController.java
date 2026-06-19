package com.mediprice.controller;

import com.mediprice.model.User;
import com.mediprice.service.AuthService;
import com.mediprice.service.ServiceException;
import com.mediprice.util.SceneManager;
import com.mediprice.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Login screen.
 */
public class LoginController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button guestButton;
    @FXML private Label errorLabel;
    @FXML private Label titleLabel;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Allow Enter key to trigger login
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
        errorLabel.setVisible(false);

        // Grab stage for scene switching after load
        Platform.runLater(() ->
            SceneManager.setPrimaryStage((Stage) loginButton.getScene().getWindow())
        );
    }

    @FXML
    private void handleLogin() {
        clearError();
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User user = authService.login(username, password);
            navigateToMainPanel(user);
        } catch (ServiceException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Navigation error", e);
            showError("Application error. Please restart.");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            SceneManager.switchScene("/fxml/Register.fxml");
        } catch (IOException e) {
            showError("Failed to open registration screen.");
        }
    }

    @FXML
    private void handleGuest() {
        try {
            SceneManager.switchScene("/fxml/Search.fxml");
        } catch (IOException e) {
            showError("Failed to open search screen.");
        }
    }

    private void navigateToMainPanel(User user) throws IOException {
        if ("ADMIN".equals(user.getRole())) {
            SceneManager.switchScene("/fxml/AdminPanel.fxml");
        } else {
            SceneManager.switchScene("/fxml/UserPanel.fxml");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
    }
}
