package com.govsim.govsim.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.govsim.govsim.database.DBManager;

import java.io.IOException;

/**
 * MainApp — Entry point for GovSim JavaFX application.
 * Manages scene switching between Login and Dashboard.
 */
public class MainApp extends Application {

    private Stage primaryStage;

    // ─────────────────────────────────────────────────────
    // START
    // ─────────────────────────────────────────────────────

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        System.out.println("═══════════════════════════════════════");
        System.out.println("       GovSim — Starting Application");
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
            System.err.println("⚠ Database connection failed: " + e.getMessage());
        }

        showLogin();

        primaryStage.setTitle("GovSim — Government Simulation");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    // ─────────────────────────────────────────────────────
    // SCENES
    // ─────────────────────────────────────────────────────

    /** Show login screen */
    public void showLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml"));
        BorderPane root = loader.load();

        LoginController controller = loader.getController();
        controller.setMainApp(this);

        primaryStage.setScene(new Scene(root));
    }

    /** Show dashboard after successful login */
    public void showDashboard(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml"));
            BorderPane root = loader.load();

            DashboardController controller = loader.getController();
            controller.setMainApp(this);
            controller.setUserId(userId);

            primaryStage.setScene(new Scene(root));

        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Logout — return to login screen */
    public void logout() {
        try {
            showLogin();
        } catch (IOException e) {
            System.err.println("Error returning to login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────────────

    public static void main(String[] args) {
        launch(args);
    }
}