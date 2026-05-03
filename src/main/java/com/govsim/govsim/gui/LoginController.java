package com.govsim.govsim.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.govsim.govsim.database.UserDAO;

/**
 * Controller for the login screen
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private MainApp mainApp;
    private int currentUserId = -1;

    /**
     * Sets the main app reference
     * @param mainApp the main application
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Handles login button action
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        // Authenticate user
        int userId = UserDAO.authenticateUser(username, password);

        if (userId != -1) {
            currentUserId = userId;
            errorLabel.setText("");
            System.out.println("Login successful for user: " + username);
            
            // Switch to dashboard
            if (mainApp != null) {
                mainApp.showDashboard(userId);
            }
        } else {
            showError("Invalid username or password");
            clearFields();
        }
    }

    /**
     * Handles register button action
     */
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        // Register user
        if (UserDAO.registerUser(username, password)) {
            showError(""); // Clear error
            System.out.println("Registration successful for user: " + username);
            
            // Auto-login after registration
            int userId = UserDAO.authenticateUser(username, password);
            if (userId != -1) {
                currentUserId = userId;
                if (mainApp != null) {
                    mainApp.showDashboard(userId);
                }
            }
        } else {
            showError("Username already exists or registration failed");
            clearFields();
        }
    }

    /**
     * Displays error message
     * @param message the error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
    }

    /**
     * Clears input fields
     */
    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }

    /**
     * Gets the current logged-in user ID
     * @return user ID
     */
    public int getCurrentUserId() {
        return currentUserId;
    }
}


