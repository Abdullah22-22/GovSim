// AlertController for managing alert displays in the GUI
package com.govsim.govsim.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Timer;
import com.govsim.govsim.ministry.Ministry;
import com.govsim.govsim.president.President;
import com.govsim.govsim.ministry.DefenseMinistry;
import com.govsim.govsim.ministry.HealthMinistry;

/**
 * Controller for alert display and styling
 */
public class AlertController {
    
    @FXML
    private Label alertLabel;

    private Timer timer;
    private DashboardController dashboardController;

    /**
     * Sets the dashboard controller reference
     * @param dashboardController the dashboard controller
     */
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    /**
     * Shows an alert with a specific message and severity level
     * @param message the alert message
     * @param severity the severity level (normal, dangerous, warning)
     */
    public void showAlert(String message, String severity) {
        if (alertLabel != null) {
            alertLabel.setText(message);
            switch (severity.toLowerCase()) {
                case "normal":
                    alertLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10;");
                    break;
                case "dangerous":
                    alertLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10;");
                    break;
                case "warning":
                    alertLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 10;");
                    break;
                default:
                    alertLabel.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10;");
                    break;
            }
        }
    }

    /**
     * Clears the alert
     */
    public void clearAlert() {
        if (alertLabel != null) {
            alertLabel.setText("");
            alertLabel.setStyle("");
        }
    }
}
