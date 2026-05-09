package com.govsim.govsim.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Minister;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.model.City;
import com.govsim.govsim.president.DecisionOption;
import com.govsim.govsim.simulation.SimuEngineGUI;
import com.govsim.govsim.simulation.SimuEngineGUI.SimulationListener;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * DashboardController — main game controller.
 * Manages the simulation timer, UI updates, sidebars, and popups.
 */
public class DashboardController implements SimulationListener {

    // ── Header stats ──
    @FXML private Label budgetLabel;
    @FXML private Label satisfactionLabel;
    @FXML private Label populationLabel;
    @FXML private Label monthYearLabel;
    @FXML private Label dayLabel;

    // ── Building health labels (city map) ──
    @FXML private Label interiorStatusLabel;
    @FXML private Label defenseStatusLabel;
    @FXML private Label financeStatusLabel;
    @FXML private Label populationStatusLabel;
    @FXML private Label healthStatusLabel;

    // ── Alert banner ──
    @FXML private HBox  alertBanner;
    @FXML private Label alertLabel;

    // ── Control buttons ──
    @FXML private Button pauseBtn;
    @FXML private Button resumeBtn;
    @FXML private Button speedBtn;

    // ── Danger light ──
    @FXML private Region dangerLight;
    @FXML private Label  dangerLightLabel;

    // ── Sidebar root containers (injected from dashboard.fxml) ──
    @FXML private VBox eventsSidebarRoot;
    @FXML private VBox aiSidebarRoot;

    // ── Sidebar controllers ──
    private EventsSidebarController eventsSidebar;
    private AISidebarController     aiSidebar;

    // ── App state ──
    private MainApp       mainApp;
    private int           userId               = -1;
    private SimuEngineGUI engine;
    private Timer         simulationTimer;
    private Timer         pulseTimer;
    private int           selectedDecisionIndex = -1;
    private boolean       isPaused             = false;
    private int           speedLevel           = 1; // 1x 2x 4x
    private boolean       gameEnded            = false;

    private static final long BASE_INTERVAL  = 4000;
    private static final long START_DELAY    = 3000;

    // ─────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────

    public void setMainApp(MainApp app) { this.mainApp = app; }

    public void setUserId(int userId) {
        this.userId = userId;
        initializeDashboard();
    }

    private void initializeDashboard() {
        engine = new SimuEngineGUI();
        engine.initializeForGUI(userId);
        engine.setSimulationListener(this);

        loadSidebars();
        updateStats();
        updateBuildingHealth();
        updateButtonStates();
        showAlert("Simulation Started", "green");
        startSimulation();
    }

    // ─────────────────────────────────────────────────────
    // SIDEBAR LOADING
    // ─────────────────────────────────────────────────────

    private void loadSidebars() {
        try {
            // Left sidebar — Daily events
            FXMLLoader eventsLoader = new FXMLLoader(
                    getClass().getResource("/fxml/events_sidebar.fxml"));
            VBox eventsRoot = eventsLoader.load();
            eventsSidebar = eventsLoader.getController();
            eventsSidebarRoot.getChildren().setAll(eventsRoot);
            VBox.setVgrow(eventsRoot, Priority.ALWAYS);

            // Right sidebar — AI advisor
            FXMLLoader aiLoader = new FXMLLoader(
                    getClass().getResource("/fxml/ai_sidebar.fxml"));
            VBox aiRoot = aiLoader.load();
            aiSidebar = aiLoader.getController();
            aiSidebarRoot.getChildren().setAll(aiRoot);
            VBox.setVgrow(aiRoot, Priority.ALWAYS);

            // Listen for user's decision selection
            aiSidebar.setOnDecisionSelected(index -> selectedDecisionIndex = index);

            // Initial states
            aiSidebar.showWaiting();
            eventsSidebar.update(engine.getMinistries());

        } catch (IOException e) {
            System.err.println("[Dashboard] Error loading sidebars: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    // SIMULATION TIMER
    // ─────────────────────────────────────────────────────

    private void startSimulation() {
        isPaused = false;
        long interval = BASE_INTERVAL / speedLevel;

        simulationTimer = new Timer(true);
        simulationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused && !gameEnded) {
                    Platform.runLater(() -> {
                        engine.advanceDayForGUI();
                        updateStats();
                        updateBuildingHealth();
                        if (eventsSidebar != null)
                            eventsSidebar.update(engine.getMinistries());
                        if (engine.checkGameOverForGUI())
                            stopSimulation();
                    });
                }
            }
        }, START_DELAY, interval);

        updateButtonStates();
    }

    private void stopSimulation() {
        if (simulationTimer != null) {
            simulationTimer.cancel();
            simulationTimer.purge();
            simulationTimer = null;
        }
    }

    // ─────────────────────────────────────────────────────
    // BUTTON HANDLERS
    // ─────────────────────────────────────────────────────

    @FXML private void handlePause() {
        isPaused = true;
        updateButtonStates();
        showAlert("SIMULATION PAUSED", "yellow");
    }

    @FXML private void handleResume() {
        isPaused = false;
        updateButtonStates();
        showAlert("SIMULATION RESUMED", "green");
    }

    @FXML private void handleSpeedToggle() {
        if      (speedLevel == 1) speedLevel = 2;
        else if (speedLevel == 2) speedLevel = 4;
        else                      speedLevel = 1;
        stopSimulation();
        startSimulation();
        showAlert(speedLevel + "x SPEED", "blue");
        updateButtonStates();
    }

    @FXML private void handleConfirmDecision() {
        if (selectedDecisionIndex < 0) {
            showAlert("Please select a decision first", "yellow");
            return;
        }
        if (engine.getCurrentOptions() != null) {
            engine.applyEventDecisionForGUI(selectedDecisionIndex);
        } else {
            showAlert("No active event to decide on", "yellow");
        }
    }

    private void updateButtonStates() {
        pauseBtn.getStyleClass().setAll(
                isPaused ? "btn-pause-disabled" : "btn-pause");
        pauseBtn.setDisable(isPaused);

        resumeBtn.getStyleClass().setAll(
                !isPaused ? "btn-resume-disabled" : "btn-resume");
        resumeBtn.setDisable(!isPaused);

        speedBtn.setText(speedLevel + "x");
        speedBtn.getStyleClass().setAll(
                speedLevel > 1 ? "btn-speed-active" : "btn-speed-normal");
    }

    // ─────────────────────────────────────────────────────
    // SIMULATION LISTENER — callbacks from SimuEngineGUI
    // ─────────────────────────────────────────────────────

    @Override
    public void onDayAdvanced(int day, int month, int year) {
        Platform.runLater(this::updateStats);
    }

    @Override
    public void onDangerousEvent(Event event, DecisionOption[] options) {
        Platform.runLater(() -> {
            isPaused = true;
            updateButtonStates();
            showAlert("DANGEROUS: " + event.getMinistry() + " — " + event.getDescription(), "red");
            flashDangerAlert();
            startDangerPulse();
            if (aiSidebar != null) aiSidebar.showDangerousEvent(event, options);
        });
    }

    @Override
    public void onDecisionApplied(DecisionOption chosen) {
        Platform.runLater(() -> {
            stopDangerPulse();
            isPaused = false;
            updateButtonStates();
            showAlert(chosen.title + ": " + chosen.description, "green");
            selectedDecisionIndex = -1;
            if (aiSidebar != null) aiSidebar.showWaiting();
            updateStats();
            updateBuildingHealth();
        });
    }

    @Override
    public void onGameOver(String reason) {
        Platform.runLater(() -> {
            if (gameEnded) return;
            gameEnded = true;
            stopSimulation();
            stopDangerPulse();
            // Delete all saved data for this user
            engine.deleteSave();
            showGameEndPopup(reason);
        });
    }

    @Override
    public void onMonthlyReport(List<Report> reports) {
        Platform.runLater(() -> {
            isPaused = true;
            updateButtonStates();
            showMonthlyReportPopup(reports);
        });
    }

    @Override
    public void onAnnualReview(List<Minister> ministers, int year) {
        // Annual review is now triggered after monthly popup closes
        // via engine.consumePendingAnnualYear() — no direct handling needed here
    }

    @Override public void onStatsUpdated() {}
    @Override public void onMonthEnd()     {}

    // ─────────────────────────────────────────────────────
    // UI UPDATES
    // ─────────────────────────────────────────────────────

    private void updateStats() {
        if (engine == null || engine.getCity() == null) return;
        City city = engine.getCity();
        budgetLabel.setText(String.format("$%,.0f", city.getBudget()));
        satisfactionLabel.setText(String.format("%.0f%%", city.getSatisfaction()));
        populationLabel.setText(String.format("%,d", city.getPopulation()));
        monthYearLabel.setText(city.getMonth() + " / Year " + city.getYear());
        dayLabel.setText(engine.getCurrentDay() + " / " + engine.getDaysInMonth());
    }

    private void updateBuildingHealth() {
        setHealthLabel(interiorStatusLabel,   engine.getInteriorHealth());
        setHealthLabel(defenseStatusLabel,    engine.getDefenseHealth());
        setHealthLabel(financeStatusLabel,    engine.getFinanceHealth());
        setHealthLabel(populationStatusLabel, engine.getPopulationHealth());
        setHealthLabel(healthStatusLabel,     engine.getHealthHealth());
    }

    private void setHealthLabel(Label label, int health) {
        label.getStyleClass().removeAll(
                "building-health-good",
                "building-health-warning",
                "building-health-danger");
        label.getStyleClass().add(
                health >= 80 ? "building-health-good" :
                        health >= 50 ? "building-health-warning" :
                                "building-health-danger");
        label.setText(health + "%");
    }

    // ─────────────────────────────────────────────────────
    // DANGER LIGHT (pulsing red when dangerous event active)
    // ─────────────────────────────────────────────────────

    private void startDangerPulse() {
        if (dangerLight == null) return;
        final boolean[] on = {true};
        pulseTimer = new Timer(true);
        pulseTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    dangerLight.setStyle(on[0]
                            ? "-fx-background-color: #ff0000; -fx-background-radius: 50;" +
                            " -fx-min-width: 18; -fx-min-height: 18;" +
                            " -fx-max-width: 18; -fx-max-height: 18;" +
                            " -fx-effect: dropshadow(gaussian, #ff0000, 15, 0.8, 0, 0);"
                            : "-fx-background-color: #3a3a3a; -fx-background-radius: 50;" +
                            " -fx-min-width: 18; -fx-min-height: 18;" +
                            " -fx-max-width: 18; -fx-max-height: 18;");
                    if (dangerLightLabel != null)
                        dangerLightLabel.setStyle(on[0]
                                ? "-fx-text-fill: #ff0000; -fx-font-weight: bold;"
                                : "-fx-text-fill: #3a3a3a; -fx-font-weight: bold;");
                    on[0] = !on[0];
                });
            }
        }, 0, 500);
    }

    private void stopDangerPulse() {
        if (pulseTimer != null) {
            pulseTimer.cancel();
            pulseTimer = null;
        }
        if (dangerLight == null) return;
        Platform.runLater(() -> {
            dangerLight.getStyleClass().setAll("danger-light-off");
            if (dangerLightLabel != null)
                dangerLightLabel.setStyle(
                        "-fx-text-fill: #3a3a3a; -fx-font-weight: bold;");
        });
    }

    // ─────────────────────────────────────────────────────
    // ALERT BANNER
    // ─────────────────────────────────────────────────────

    private void showAlert(String message, String type) {
        alertLabel.setText(message);
        alertBanner.getStyleClass().removeAll(
                "alert-banner-green", "alert-banner-red",
                "alert-banner-yellow", "alert-banner-blue");
        switch (type) {
            case "green"  -> alertBanner.getStyleClass().add("alert-banner-green");
            case "red"    -> alertBanner.getStyleClass().add("alert-banner-red");
            case "yellow" -> alertBanner.getStyleClass().add("alert-banner-yellow");
            case "blue"   -> alertBanner.getStyleClass().add("alert-banner-blue");
        }
    }

    private void flashDangerAlert() {
        final int[] count = {0};
        Timer flashTimer = new Timer(true);
        flashTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    alertBanner.getStyleClass().removeAll(
                            "alert-banner-flash1", "alert-banner-flash2");
                    alertBanner.getStyleClass().add(
                            count[0] % 2 == 0
                                    ? "alert-banner-flash1"
                                    : "alert-banner-flash2");
                    if (++count[0] >= 6) flashTimer.cancel();
                });
            }
        }, 0, 200);
    }

    // ─────────────────────────────────────────────────────
    // POPUPS
    // ─────────────────────────────────────────────────────

    private void showMonthlyReportPopup(List<Report> reports) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/monthly_report.fxml"));
            VBox root = loader.load();
            MonthlyReportController ctrl = loader.getController();

            Stage popup = buildPopup("Monthly Government Report", root);
            ctrl.setData(popup, reports, engine.getCity(), () -> {
                updateStats();
                showAlert("Monthly decisions applied", "green");
                // callback fires on close — handled below
            });
            popup.showAndWait();

            // After monthly popup closed — check for pending annual review
            int annualYear = engine.consumePendingAnnualYear();
            if (annualYear > 0) {
                // Keep block & pause active until annual review done too
                showAnnualReviewPopup(engine.getMinisters(), annualYear);
            } else {
                engine.setBlockAdvance(false);
                isPaused = false;
                updateButtonStates();
            }

        } catch (IOException e) {
            System.err.println("[Dashboard] Monthly popup error: " + e.getMessage());
            engine.setBlockAdvance(false);
            isPaused = false;
            updateButtonStates();
        }
    }

    private void showAnnualReviewPopup(List<Minister> ministers, int year) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/annual_review.fxml"));
            VBox root = loader.load();
            AnnualReviewController ctrl = loader.getController();

            Stage popup = buildPopup("Annual Minister Review", root);
            ctrl.setData(popup, ministers, engine.getCity(), year, () -> {
                updateStats();
                showAlert("Annual review completed", "yellow");
            });
            popup.showAndWait();
            // Unblock engine after annual review popup closes
            engine.setBlockAdvance(false);
            isPaused = false;
            updateButtonStates();

        } catch (IOException e) {
            System.err.println("[Dashboard] Annual popup error: " + e.getMessage());
            engine.setBlockAdvance(false);
            isPaused = false;
            updateButtonStates();
        }
    }

    /** Creates a modal popup Stage with the given root node */
    private Stage buildPopup(String title, VBox root) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(title);
        popup.setScene(new Scene(root));
        popup.setResizable(false);
        return popup;
    }

    // ─────────────────────────────────────────────────────
    // GAME END POPUP (Win / Loss)
    // ─────────────────────────────────────────────────────

    private void showGameEndPopup(String reason) {
        boolean isWin = reason.startsWith("YOU WIN");

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(isWin ? "🏆 Victory!" : "💀 Game Over");
        popup.setResizable(false);

        // ── Emoji / icon ──
        Label icon = new Label(isWin ? "🏆" : "💀");
        icon.setStyle("-fx-font-size: 72; -fx-padding: 0 0 8 0;");

        // ── Title ──
        Label title = new Label(isWin ? "YOU WIN!" : "GAME OVER");
        title.setStyle(
                "-fx-font-size: 32; -fx-font-weight: bold; " +
                        "-fx-text-fill: " + (isWin ? "#ffd600" : "#ff4444") + ";"
        );

        // ── Reason message ──
        Label msg = new Label(reason);
        msg.setStyle(
                "-fx-font-size: 14; -fx-text-fill: #b0c4de; " +
                        "-fx-wrap-text: true; -fx-text-alignment: center;"
        );
        msg.setMaxWidth(380);
        msg.setWrapText(true);

        // ── Subtitle ──
        Label sub = new Label(isWin
                ? "Congratulations! You successfully governed the city for 3 years."
                : "Your save data has been erased. Try again from the beginning!");
        sub.setStyle("-fx-font-size: 12; -fx-text-fill: #7a8fa8; -fx-wrap-text: true; -fx-text-alignment: center;");
        sub.setMaxWidth(380);
        sub.setWrapText(true);

        // ── Button ──
        Button backBtn = new Button("Back to Login");
        backBtn.setStyle(
                "-fx-background-color: " + (isWin ? "#ffd600" : "#c0392b") + "; " +
                        "-fx-text-fill: #0a1220; -fx-font-weight: bold; " +
                        "-fx-font-size: 14; -fx-padding: 10 32 10 32; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;"
        );
        backBtn.setOnAction(e -> {
            popup.close();
            if (mainApp != null) mainApp.logout();
        });

        // ── Layout ──
        VBox box = new VBox(16, icon, title, msg, sub, backBtn);
        box.setStyle(
                "-fx-background-color: #0d1f3c; " +
                        "-fx-padding: 40 50 40 50; " +
                        "-fx-alignment: center;"
        );
        box.setAlignment(javafx.geometry.Pos.CENTER);

        popup.setScene(new Scene(box));
        popup.setOnCloseRequest(e -> {
            if (mainApp != null) mainApp.logout();
        });
        popup.show();
    }

    // ─────────────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────────────

    public void logout() {
        stopSimulation();
        stopDangerPulse();
        if (mainApp != null) mainApp.logout();
    }
}