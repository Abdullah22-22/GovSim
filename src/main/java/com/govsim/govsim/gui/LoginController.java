<<<<<<< Updated upstream
/*  
=======
package com.govsim.govsim.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import com.govsim.govsim.database.UserDAO;
import com.govsim.govsim.model.User;

/**
 * LoginController — handles login and registration UI.
 * Delegates all database operations to UserDAO.
 */
public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    private MainApp         mainApp;
    private final UserDAO   userDAO = new UserDAO();

    // ─────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
>>>>>>> Stashed changes

    // ─────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────


<<<<<<< Updated upstream
This is the second 
*/
=======
        if (!validateInput(username, password)) return;

        User user = userDAO.login(username, password);

        if (user == null) {
            showError("Invalid username or password");
            clearFields();
            return;
        }

        loginSuccess(user);
    }

    // ─────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (!validateInput(username, password)) return;

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        boolean success = userDAO.register(new User(username, password));

        if (!success) {
            showError("Username already exists");
            clearFields();
            return;
        }

        // Auto-login after registration
        User user = userDAO.login(username, password);
        if (user != null) loginSuccess(user);
    }

    // ─────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────

    private void loginSuccess(User user) {
        showError("");
        if (mainApp != null) mainApp.showDashboard(user.getId());
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }
}
>>>>>>> Stashed changes
