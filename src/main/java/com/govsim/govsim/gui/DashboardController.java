package com.govsim.govsim.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.Cursor;
import javafx.application.Platform;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Severity;
import com.govsim.govsim.model.City;
import com.govsim.govsim.ministry.Ministry;
import com.govsim.govsim.president.DecisionOption;
import com.govsim.govsim.simulation.SimuEngine;
import com.govsim.govsim.simulation.SimuEngine.SimulationListener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class DashboardController implements SimulationListener {

    @FXML private Label budgetLabel;
    @FXML private Label satisfactionLabel;
    @FXML private Label populationLabel;
    @FXML private Label monthYearLabel;
    @FXML private Label dayLabel;

    @FXML private Label interiorStatusLabel;
    @FXML private Label defenseStatusLabel;
    @FXML private Label financeStatusLabel;
    @FXML private Label populationStatusLabel;
    @FXML private Label healthStatusLabel;

    @FXML private HBox  alertBanner;
    @FXML private Label alertLabel;
    @FXML private Label advisorDescription;

    @FXML private VBox ministriesContainer;
    @FXML private VBox incidentsContainer;
    @FXML private VBox decisionsContainer;

    @FXML private Button pauseBtn;
    @FXML private Button resumeBtn;
    @FXML private Button speedBtn;

    private MainApp mainApp;
    private int userId = -1;

    // GAME ENGINE
    private SimuEngine engine;
    private Timer simulationTimer;

    private int selectedDecisionIndex = -1;

    // Speed and pause controls
    private boolean isPaused = false;
    private boolean isSpeedX2 = false;
    private long baseInterval = 4000;  // ms between day advances
    private long pauseInterval = 3000; // initial delay


    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        initializeDashboard();
    }


    private void initializeDashboard() {
        // Create the game engine
        engine = new SimuEngine();
        engine.initializeForGUI(userId);
        engine.setSimulationListener(this);

        updateStats();
        updateBuildingStatus();
        populateMinistries();
        populateIncidents();
        populateDecisions(null);
        updateAdvisor();
        updateButtonStates();
        showAlert("✓ Simulation Started", "#27ae60");

        startSimulation();
    }

    private void startSimulation() {
        isPaused = false;
        simulationTimer = new Timer();
        long interval = isSpeedX2 ? baseInterval / 2 : baseInterval;
        
        simulationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused) {
                    Platform.runLater(() -> {
                        engine.advanceDayForGUI();
                        updateStats();
                        updateBuildingStatus();
                        populateMinistries();
                        populateIncidents();
                        updateAdvisor();
                        if (engine.checkGameOverForGUI()) {
                            stopSimulation();
                        }
                    });
                }
            }
        }, pauseInterval, interval);

        updateButtonStates();
    }

    // ═════════════════════════════════════════════════════
    // PAUSE / RESUME / SPEED CONTROLS
    // ═════════════════════════════════════════════════════

    @FXML
    private void handlePause() {
        isPaused = true;
        updateButtonStates();
        showAlert("⏸ SIMULATION PAUSED", "#f39c12");
    }

    @FXML
    private void handleResume() {
        isPaused = false;
        updateButtonStates();
        showAlert("▶ SIMULATION RESUMED", "#27ae60");
    }

    @FXML
    private void handleSpeedToggle() {
        isSpeedX2 = !isSpeedX2;
        
        // Restart timer with new interval
        stopSimulation();
        startSimulation();
        
        String speedText = isSpeedX2 ? "2× SPEED" : "1× SPEED";
        showAlert(speedText, "#3498db");
        updateButtonStates();
    }

    private void updateButtonStates() {
        // Pause button - enabled only when running
        pauseBtn.setDisable(isPaused);
        pauseBtn.setStyle(isPaused ? 
            "-fx-font-size: 12; -fx-background-color: #7f8fa3; -fx-text-fill: #ffffff; -fx-opacity: 0.5;" :
            "-fx-font-size: 12; -fx-background-color: #3498db; -fx-text-fill: #ffffff;");

        // Resume button - enabled only when paused
        resumeBtn.setDisable(!isPaused);
        resumeBtn.setStyle(!isPaused ? 
            "-fx-font-size: 12; -fx-background-color: #e74c3c; -fx-text-fill: #ffffff; -fx-opacity: 0.5;" :
            "-fx-font-size: 12; -fx-background-color: #e74c3c; -fx-text-fill: #ffffff;");

        // Speed button - highlight when x2 is active
        speedBtn.setStyle(isSpeedX2 ? 
            "-fx-font-size: 12; -fx-background-color: #f1c40f; -fx-text-fill: #333333; -fx-font-weight: bold;" :
            "-fx-font-size: 12; -fx-background-color: #1a1f3a; -fx-text-fill: #ffffff;");
    }

    // ═════════════════════════════════════════════════════
    // LISTENER CALLBACKS FROM ENGINE
    // ═════════════════════════════════════════════════════

    @Override
    public void onDayAdvanced(int day, int month, int year) {
        Platform.runLater(this::updateStats);
    }

    @Override
    public void onDangerousEvent(Event event, DecisionOption[] options) {
        Platform.runLater(() -> {
            populateDecisions(options);
            String alertText = "⚠ DANGEROUS: " + event.getMinistry() + " — " + event.getDescription();
            showAlert(alertText, "#e74c3c");
        });
    }

    @Override
    public void onDecisionApplied(DecisionOption chosen) {
        Platform.runLater(() -> {
            showAlert("✓ " + chosen.title + ": " + chosen.description, "#27ae60");
            selectedDecisionIndex = -1;
            populateDecisions(null);
            updateStats();
            updateBuildingStatus();
        });
    }

    @Override
    public void onGameOver(String reason) {
        Platform.runLater(() -> {
            showAlert(reason, "#e74c3c");
            stopSimulation();
        });
    }

    @Override
    public void onStatsUpdated() {
        // Stats will be updated by the timer callback
    }

    @Override
    public void onMonthEnd() {
        // Month-end logic handled
    }

    // ═════════════════════════════════════════════════════
    // UI UPDATE METHODS (View Only)
    // ═════════════════════════════════════════════════════

    private void updateStats() {
        if (engine == null || engine.getCity() == null) return;
        City city = engine.getCity();
        
        budgetLabel.setText("$" + String.format("%,.0f", city.getBudget()));
        satisfactionLabel.setText(String.format("%.0f%%", city.getSatisfaction()));
        populationLabel.setText(String.format("%,d", city.getPopulation()));
        monthYearLabel.setText(city.getMonth() + " / Year " + city.getYear());
        dayLabel.setText(engine.getCurrentDay() + " / " + engine.getDaysInMonth());
    }

    private void updateBuildingStatus() {
        updateLabelColor(interiorStatusLabel,   engine.getInteriorHealth());
        updateLabelColor(defenseStatusLabel,    engine.getDefenseHealth());
        updateLabelColor(financeStatusLabel,    engine.getFinanceHealth());
        updateLabelColor(populationStatusLabel, engine.getPopulationHealth());
        updateLabelColor(healthStatusLabel,     engine.getHealthHealth());

        interiorStatusLabel.setText(engine.getInteriorHealth() + "%");
        defenseStatusLabel.setText(engine.getDefenseHealth() + "%");
        financeStatusLabel.setText(engine.getFinanceHealth() + "%");
        populationStatusLabel.setText(engine.getPopulationHealth() + "%");
        healthStatusLabel.setText(engine.getHealthHealth() + "%");
    }

    private void updateLabelColor(Label label, int health) {
        String color = health >= 80 ? "#27ae60" : health >= 50 ? "#f1c40f" : "#e74c3c";
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12; -fx-font-weight: bold;");
    }

    private void populateMinistries() {
        ministriesContainer.getChildren().clear();
        String[] names     = {"🏠 Interior", "⚔ Defense", "💰 Finance", "👥 Population", "🏥 Health"};
        String[] subtitles = {"Police, Prison", "Army, Borders", "Budget, Tax", "Citizens", "Hospitals"};
        int[]    health    = {engine.getInteriorHealth(), engine.getDefenseHealth(), engine.getFinanceHealth(), 
                              engine.getPopulationHealth(), engine.getHealthHealth()};

        for (int i = 0; i < names.length; i++) {
            HBox item = createMinistryListItem(names[i], subtitles[i], health[i]);
            ministriesContainer.getChildren().add(item);
        }
    }

    private HBox createMinistryListItem(String name, String subtitle, int healthVal) {
        HBox box = new HBox(10);
        box.setStyle("-fx-background-color: #1a1f3a; -fx-padding: 8; -fx-border-color: #2a3f5f; -fx-border-width: 1;");

        VBox info = new VBox(2);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-font-size: 9; -fx-text-fill: #7f8fa3;");
        info.getChildren().addAll(nameLabel, subLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String color = healthVal >= 80 ? "#27ae60" : healthVal >= 50 ? "#f1c40f" : "#e74c3c";
        Label hLabel = new Label(healthVal + "%");
        hLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11; -fx-font-weight: bold;");

        box.getChildren().addAll(info, spacer, hLabel);
        return box;
    }

    private void populateIncidents() {
        incidentsContainer.getChildren().clear();

        for (Ministry m : engine.getMinistries()) {
            List<Event> log = m.getEventLog();
            if (!log.isEmpty()) {
                Event last = log.get(log.size() - 1);
                Label lbl  = new Label("• " + m.getName() + ": " + last.getDescription());
                String color = last.getSeverity() == Severity.DANGEROUS ? "#e74c3c" : "#f1c40f";
                lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 9; -fx-wrap-text: true;");
                incidentsContainer.getChildren().add(lbl);
            }
        }

        if (incidentsContainer.getChildren().isEmpty()) {
            Label ok = new Label("✓ No incidents");
            ok.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 9;");
            incidentsContainer.getChildren().add(ok);
        }
    }

    private void populateDecisions(DecisionOption[] options) {
        decisionsContainer.getChildren().clear();
        selectedDecisionIndex = -1;

        if (options == null) {
            Label wait = new Label("Waiting for a dangerous event...");
            wait.setStyle("-fx-text-fill: #7f8fa3; -fx-font-size: 10; -fx-wrap-text: true;");
            decisionsContainer.getChildren().add(wait);
            return;
        }

        for (int i = 0; i < options.length; i++) {
            final int index = i;
            DecisionOption opt = options[i];

            VBox card = new VBox(4);
            card.setStyle("-fx-background-color: #0f1419; -fx-padding: 8; -fx-border-color: #2a3f5f; -fx-border-width: 1;");
            card.setCursor(Cursor.HAND);

            Label titleLbl = new Label(opt.title);
            titleLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11; -fx-font-weight: bold;");
            Label descLbl = new Label(opt.description);
            descLbl.setStyle("-fx-text-fill: #7f8fa3; -fx-font-size: 9; -fx-wrap-text: true;");
            Label costLbl = new Label("Cost: €" + opt.cost);
            costLbl.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 9;");

            card.getChildren().addAll(titleLbl, descLbl, costLbl);

            card.setOnMouseClicked(e -> {
                selectedDecisionIndex = index;
                updateDecisionSelection();
            });

            decisionsContainer.getChildren().add(card);
        }
    }

    @FXML
    private void handleConfirmDecision() {
        if (selectedDecisionIndex < 0) {
            showAlert("⚠ Please select a decision first", "#f39c12");
            return;
        }

        if (engine.getCurrentOptions() != null) {
            engine.applyEventDecisionForGUI(selectedDecisionIndex);
        } else {
            showAlert("⚠ No active event to decide on", "#f39c12");
        }
    }

    private void updateDecisionSelection() {
        int idx = 0;
        for (javafx.scene.Node node : decisionsContainer.getChildren()) {
            if (node instanceof VBox) {
                String style = idx == selectedDecisionIndex
                        ? "-fx-background-color: #3498db; -fx-padding: 8; -fx-border-color: #2980b9; -fx-border-width: 2;"
                        : "-fx-background-color: #0f1419; -fx-padding: 8; -fx-border-color: #2a3f5f; -fx-border-width: 1;";
                node.setStyle(style);
            }
            idx++;
        }
    }

    private void updateAdvisor() {
        double avg = (engine.getInteriorHealth() + engine.getDefenseHealth() + engine.getFinanceHealth()
                      + engine.getPopulationHealth() + engine.getHealthHealth()) / 5.0;
        String msg, color;
        if (avg >= 80)      { msg = "All systems operational. Continue monitoring."; color = "#27ae60"; }
        else if (avg >= 60) { msg = "Minor issues detected. Consider preventive measures."; color = "#f1c40f"; }
        else if (avg >= 40) { msg = "Multiple concerns. Quick action recommended!"; color = "#f39c12"; }
        else                { msg = "CRITICAL: Urgent intervention needed!"; color = "#e74c3c"; }

        advisorDescription.setText(msg);
        advisorDescription.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10; -fx-wrap-text: true;");
    }

    private void showAlert(String message, String bgColor) {
        alertLabel.setText(message);
        alertBanner.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 15; -fx-alignment: center;");
    }

    private void stopSimulation() {
        if (simulationTimer != null) {
            simulationTimer.cancel();
            simulationTimer.purge();
            simulationTimer = null;
        }
    }

    // ─────────────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────────────

    public void logout() {
        stopSimulation();
        if (mainApp != null) mainApp.logout();
    }
}
