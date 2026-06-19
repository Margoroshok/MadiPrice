package com.mediprice.controller;

import com.mediprice.service.AuthService;
import com.mediprice.service.ServiceException;
import com.mediprice.util.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for user registration.
 */
public class RegisterController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        clearMessages();
        try {
            authService.register(
                    usernameField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    confirmPasswordField.getText()
            );
            successLabel.setText("Account created! You can now log in.");
            successLabel.setVisible(true);
            clearForm();
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.switchScene("/fxml/Login.fxml");
        } catch (IOException e) {
            showError("Navigation error.");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void clearMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    private void clearForm() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}
