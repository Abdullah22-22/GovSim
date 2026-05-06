package com.govsim.govsim.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.govsim.govsim.database.DBManager;

import java.io.IOException;

/**
 * Main entry point for the GovSim application
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private LoginController loginController;
    private DashboardController dashboardController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        // App start log
        System.out.println("═══════════════════════════════════════");
        System.out.println("       GovSim - Starting Application");
        System.out.println("═══════════════════════════════════════");

        // Database connection check
        System.out.println("\n📡 Connecting to database...");

        try {
            if (DBManager.getConnection() != null) {
                System.out.println("✓ Database connection successful\n");
            } else {
                System.err.println("⚠ Database connection failed.");
            }
        } catch (Exception e) {
            System.err.println("⚠ Database connection failed.");
            System.err.println("  " + e.getMessage());
        }

        // Load login screen
        showLogin();

        primaryStage.setTitle("GovSim - Government Simulation");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Show login screen
     */
    private void showLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/login.fxml"));

        BorderPane loginPage = loader.load();

        loginController = loader.getController();
        loginController.setMainApp(this);

        Scene scene = new Scene(loginPage);
        primaryStage.setScene(scene);
    }

    /**
     * Show dashboard after login
     */
    public void showDashboard(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/dashboard.fxml"));

            BorderPane dashboardPage = loader.load();

            dashboardController = loader.getController();
            dashboardController.setMainApp(this);
            dashboardController.setUserId(userId);

            Scene scene = new Scene(dashboardPage);
            primaryStage.setScene(scene);

        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Logout back to login screen
     */
    public void logout() {
        try {
            showLogin();
        } catch (IOException e) {
            System.err.println("Error returning to login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        launch(args);
    }
}