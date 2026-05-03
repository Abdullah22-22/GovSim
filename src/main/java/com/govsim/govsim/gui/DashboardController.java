package com.govsim.govsim.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.Cursor;
import javafx.application.Platform;

import com.govsim.govsim.ministry.Ministry;
import com.govsim.govsim.ministry.DefenseMinistry;
import com.govsim.govsim.ministry.HealthMinistry;
import com.govsim.govsim.ministry.InteriorMinistry;
import com.govsim.govsim.ministry.FinanceMinistry;
import com.govsim.govsim.ministry.PopulationMinistry;
import com.govsim.govsim.president.President;
import com.govsim.govsim.model.City;
import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Severity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller for the clean city map dashboard with working event system
 */
public class DashboardController {

    @FXML
    private Label budgetLabel;
    @FXML
    private Label satisfactionLabel;
    @FXML
    private Label populationLabel;
    @FXML
    private Label monthYearLabel;
    @FXML
    private Label dayLabel;

    @FXML
    private Label interiorStatusLabel;
    @FXML
    private Label defenseStatusLabel;
    @FXML
    private Label financeStatusLabel;
    @FXML
    private Label populationStatusLabel;
    @FXML
    private Label healthStatusLabel;

    @FXML
    private HBox alertBanner;
    @FXML
    private Label alertLabel;

    @FXML
    private Label advisorDescription;
    @FXML
    private VBox ministriesContainer;
    @FXML
    private VBox incidentsContainer;
    @FXML
    private VBox decisionsContainer;

    private MainApp mainApp;
    private int userId = -1;
    private Ministry defenseMinistry;
    private Ministry healthMinistry;
    private Ministry interiorMinistry;
    private Ministry financeMinistry;
    private Ministry populationMinistry;
    private President president;
    private City city;

    // Simulation state
    private int currentDay = 1;
    private int daysInMonth = 30;
    private Timer simulationTimer;
    
    // Ministry health tracking (0-100%)
    private int interiorHealth = 100;
    private int defenseHealth = 100;
    private int financeHealth = 100;
    private int populationHealth = 100;
    private int healthHealth = 100;

    // Decision tracking
    private int selectedDecisionIndex = -1;

    /**
     * Sets the main app reference
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Sets the user ID and initializes the dashboard
     */
    public void setUserId(int userId) {
        this.userId = userId;
        initializeDashboard();
    }

    /**
     * Initializes the dashboard with clean values and starts the event system
     */
    private void initializeDashboard() {
        // Initialize city with clean starting values
        city = new City(1000); // Clean start: $1000 budget
        city.setSatisfaction(100); // Full satisfaction
        
        // Initialize president with city
        president = new President(city);
        
        // Initialize all ministries
        interiorMinistry = new InteriorMinistry(president);
        defenseMinistry = new DefenseMinistry(president);
        financeMinistry = new FinanceMinistry(president);
        populationMinistry = new PopulationMinistry(president);
        healthMinistry = new HealthMinistry(president);

        // Update initial UI
        updateStats();
        updateBuildingStatus();
        populateMinistries();
        populateIncidents();
        populateDecisions();
        showAlert("✓ Simulation Started", "#27ae60");
        updateAdvisor();

        System.out.println("Dashboard initialized for user: " + userId + " | Budget: $1000 | Satisfaction: 100%");
        
        // Start the event system timer
        startSimulation();
    }

    /**
     * Starts the simulation - advances days and generates events
     */
    private void startSimulation() {
        simulationTimer = new Timer();
        simulationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    advanceDay();
                    generateRandomEvent();
                    updateStats();
                    updateBuildingStatus();
                    populateMinistries();
                    populateIncidents();
                    updateAdvisor();
                });
            }
        }, 3000, 4000); // Start after 3 seconds, then every 4 seconds
    }

    /**
     * Advances the day and month
     */
    private void advanceDay() {
        currentDay++;
        if (currentDay > daysInMonth) {
            currentDay = 1;
            city.nextMonth();
        }
        
        // Generate daily passive income based on city satisfaction
        generatePassiveIncome();
    }

    /**
     * Generates passive income daily based on city satisfaction and ministries
     */
    private void generatePassiveIncome() {
        // Base income: $150 per day
        double baseIncome = 5000;
        
        // Calculate ministry bonus based on health (better health = more income)
        double avgHealth = (interiorHealth + defenseHealth + financeHealth + populationHealth + healthHealth) / 5.0;
        double healthBonus = (avgHealth / 5000.0) * 100; 
        
        // Satisfaction multiplier (higher satisfaction = better economy)
        double satisfactionMultiplier = city.getSatisfaction() / 100.0;
        
        // Calculate total daily income
        double dailyIncome = (baseIncome + healthBonus) * satisfactionMultiplier;
        
        city.setBudget(city.getBudget() + dailyIncome);
        
        System.out.println("[Day " + currentDay + "] INCOME: +" + String.format("$%.0f", dailyIncome) + 
                           " (Base: $150, Health Bonus: $" + String.format("%.0f", healthBonus) + 
                           ", Satisfaction: " + String.format("%.0f%%", city.getSatisfaction()) + ")");
    }

    /**
     * Generates a random event from a random ministry
     */
    private void generateRandomEvent() {
        Ministry[] ministries = {interiorMinistry, defenseMinistry, financeMinistry, populationMinistry, healthMinistry};
        int randomMinistry = (int) (Math.random() * ministries.length);
        Ministry selectedMinistry = ministries[randomMinistry];
        
        // 30% chance of event per tick
        if (Math.random() < 0.3) {
            // Create random event
            Severity severity = Math.random() < 0.7 ? Severity.NORMAL : Severity.DANGEROUS;
            String[] eventTypes = {
                "Infrastructure Failure",
                "Budget Overrun",
                "Supply Shortage",
                "Personnel Issue",
                "Public Complaint"
            };
            String eventDesc = eventTypes[(int)(Math.random() * eventTypes.length)];
            String ministryName = getMinistryName(selectedMinistry);
            
            Event event = new Event(ministryName, eventDesc, severity, currentDay);
            selectedMinistry.receiveEvent(event);
            
            // Update health based on event severity
            reduceMinistryHealth(selectedMinistry, severity);
            
            // Update alert
            String alertText = severity == Severity.DANGEROUS ? 
                "⚠ DANGEROUS EVENT - " + ministryName : 
                "ℹ Event - " + ministryName;
            String alertColor = severity == Severity.DANGEROUS ? "#e74c3c" : "#f1c40f";
            showAlert(alertText, alertColor);
            
            System.out.println("[Day " + currentDay + "] EVENT: " + eventDesc + " (" + severity + ") from " + ministryName);
        }
    }

    /**
     * Gets the name of a ministry
     */
    private String getMinistryName(Ministry m) {
        if (m == interiorMinistry) return "INTERIOR";
        if (m == defenseMinistry) return "DEFENSE";
        if (m == financeMinistry) return "FINANCE";
        if (m == populationMinistry) return "POPULATION";
        if (m == healthMinistry) return "HEALTH";
        return "UNKNOWN";
    }

    /**
     * Reduces ministry health based on event severity
     */
    private void reduceMinistryHealth(Ministry m, Severity severity) {
        int reduction = severity == Severity.DANGEROUS ? 15 : 5;
        
        if (m == interiorMinistry) {
            interiorHealth = Math.max(0, interiorHealth - reduction);
        } else if (m == defenseMinistry) {
            defenseHealth = Math.max(0, defenseHealth - reduction);
        } else if (m == financeMinistry) {
            financeHealth = Math.max(0, financeHealth - reduction);
        } else if (m == populationMinistry) {
            populationHealth = Math.max(0, populationHealth - reduction);
        } else if (m == healthMinistry) {
            healthHealth = Math.max(0, healthHealth - reduction);
        }
        
        // Slowly recover if no events
        if (Math.random() < 0.2) {
            if (m == interiorMinistry) {
                interiorHealth = Math.min(100, interiorHealth + 3);
            } else if (m == defenseMinistry) {
                defenseHealth = Math.min(100, defenseHealth + 3);
            } else if (m == financeMinistry) {
                financeHealth = Math.min(100, financeHealth + 3);
            } else if (m == populationMinistry) {
                populationHealth = Math.min(100, populationHealth + 3);
            } else if (m == healthMinistry) {
                healthHealth = Math.min(100, healthHealth + 3);
            }
        }
    }

    /**
     * Updates the building status labels
     */
    private void updateBuildingStatus() {
        interiorStatusLabel.setText(interiorHealth + "");
        defenseStatusLabel.setText(defenseHealth + "");
        financeStatusLabel.setText(financeHealth + "");
        populationStatusLabel.setText(populationHealth + "");
        healthStatusLabel.setText(healthHealth + "");
        
        // Update colors based on health
        updateLabelColor(interiorStatusLabel, interiorHealth);
        updateLabelColor(defenseStatusLabel, defenseHealth);
        updateLabelColor(financeStatusLabel, financeHealth);
        updateLabelColor(populationStatusLabel, populationHealth);
        updateLabelColor(healthStatusLabel, healthHealth);
    }

    /**
     * Updates label color based on health percentage
     */
    private void updateLabelColor(Label label, int health) {
        String color;
        if (health >= 80) {
            color = "#27ae60"; // Green - OK
        } else if (health >= 50) {
            color = "#f1c40f"; // Yellow - Warning
        } else {
            color = "#e74c3c"; // Red - Danger
        }
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12; -fx-font-weight: bold;");
    }

    /**
     * Updates statistics display from City object
     */
    private void updateStats() {
        if (city != null) {
            budgetLabel.setText("$" + String.format("%,.0f", city.getBudget()));
            satisfactionLabel.setText(String.format("%.0f%%", city.getSatisfaction()));
            monthYearLabel.setText(city.getMonth() + " / Year " + city.getYear());
            dayLabel.setText(currentDay + " / " + daysInMonth);
        }
    }

    /**
     * Shows an alert message with color
     */
    private void showAlert(String message, String bgColor) {
        alertLabel.setText(message);
        alertBanner.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 15; -fx-alignment: center;");
    }

    /**
     * Populates left panel with ministries list
     */
    private void populateMinistries() {
        ministriesContainer.getChildren().clear();
        
        Ministry[] ministries = {interiorMinistry, defenseMinistry, financeMinistry, populationMinistry, healthMinistry};
        int[] health = {interiorHealth, defenseHealth, financeHealth, populationHealth, healthHealth};
        String[] names = {"🏠 Interior", "⚔ Defense", "💰 Finance", "👥 Population", "🏥 Health"};
        String[] subtitles = {"Police, Prison", "Army, Borders", "Budget, Tax", "Citizens", "Hospitals"};
        
        for (int i = 0; i < ministries.length; i++) {
            HBox ministryItem = createMinistryListItem(names[i], subtitles[i], health[i]);
            ministriesContainer.getChildren().add(ministryItem);
        }
    }

    /**
     * Creates a ministry list item
     */
    private HBox createMinistryListItem(String name, String subtitle, int healthVal) {
        HBox box = new HBox(10);
        box.setStyle("-fx-background-color: #1a1f3a; -fx-padding: 8; -fx-border-color: #2a3f5f; -fx-border-width: 1;");
        
        VBox info = new VBox(2);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 9; -fx-text-fill: #7f8fa3;");
        info.getChildren().addAll(nameLabel, subtitleLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label healthLabel = new Label(healthVal + "%");
        String color = healthVal >= 80 ? "#27ae60" : healthVal >= 50 ? "#f1c40f" : "#e74c3c";
        healthLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11; -fx-font-weight: bold;");
        
        box.getChildren().addAll(info, spacer, healthLabel);
        return box;
    }

    /**
     * Populates incidents panel
     */
    private void populateIncidents() {
        incidentsContainer.getChildren().clear();
        
        // Show recent incidents from ministries
        for (Ministry m : new Ministry[]{interiorMinistry, defenseMinistry, financeMinistry, populationMinistry, healthMinistry}) {
            if (m.getEventLog().size() > 0) {
                Event lastEvent = m.getEventLog().get(m.getEventLog().size() - 1);
                Label incident = new Label("• " + getMinistryName(m) + ": " + lastEvent.getDescription());
                incident.setStyle("-fx-text-fill: " + (lastEvent.getSeverity() == Severity.DANGEROUS ? "#e74c3c" : "#f1c40f") + "; -fx-font-size: 9;");
                incidentsContainer.getChildren().add(incident);
            }
        }
        
        if (incidentsContainer.getChildren().isEmpty()) {
            Label noIncidents = new Label("✓ No incidents");
            noIncidents.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 9;");
            incidentsContainer.getChildren().add(noIncidents);
        }
    }

    /**
     * Populates decisions panel with action options
     */
    private void populateDecisions() {
        decisionsContainer.getChildren().clear();
        selectedDecisionIndex = -1;
        
        String[] decisions = {
            "🔄 Curfew\n50% capture, -20% satisfaction",
            "👮 Deploy Police\n60% success, $5K cost",
            "🚨 Full Emergency\n85% success, $18K cost",
            "👁 Wait & Monitor\nFree, 15% per day"
        };
        
        for (int i = 0; i < decisions.length; i++) {
            final int index = i;
            Label decisionLabel = new Label(decisions[i]);
            decisionLabel.setStyle("-fx-background-color: #0f1419; -fx-padding: 8; -fx-text-fill: #ffffff; -fx-font-size: 9; -fx-border-color: #2a3f5f; -fx-border-width: 1; -fx-wrap-text: true;");
            decisionLabel.setCursor(Cursor.HAND);
            
            // Make clickable to select decision
            decisionLabel.setOnMouseClicked(e -> {
                selectedDecisionIndex = index;
                updateDecisionSelection();
            });
            
            decisionsContainer.getChildren().add(decisionLabel);
        }
    }

    /**
     * Updates visual feedback for selected decision
     */
    private void updateDecisionSelection() {
        int index = 0;
        for (javafx.scene.Node node : decisionsContainer.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (index == selectedDecisionIndex) {
                    label.setStyle("-fx-background-color: #3498db; -fx-padding: 8; -fx-text-fill: #ffffff; -fx-font-size: 9; -fx-border-color: #2980b9; -fx-border-width: 2; -fx-wrap-text: true;");
                } else {
                    label.setStyle("-fx-background-color: #0f1419; -fx-padding: 8; -fx-text-fill: #ffffff; -fx-font-size: 9; -fx-border-color: #2a3f5f; -fx-border-width: 1; -fx-wrap-text: true;");
                }
            }
            index++;
        }
    }

    /**
     * Updates the AI Advisor message
     */
    private void updateAdvisor() {
        double avgHealth = (interiorHealth + defenseHealth + financeHealth + populationHealth + healthHealth) / 5.0;
        
        if (avgHealth >= 80) {
            advisorDescription.setText("All systems operational. Continue monitoring.");
        } else if (avgHealth >= 60) {
            advisorDescription.setText("Minor issues detected. Consider preventive measures.");
        } else if (avgHealth >= 40) {
            advisorDescription.setText("Multiple concerns. Quick action recommended!");
        } else {
            advisorDescription.setText("CRITICAL: Urgent intervention needed!");
        }
        advisorDescription.setStyle("-fx-text-fill: " + (avgHealth >= 70 ? "#27ae60" : avgHealth >= 50 ? "#f1c40f" : "#e74c3c") + "; -fx-font-size: 10; -fx-wrap-text: true;");
    }

    /**
     * Handles confirm decision button - applies decision effects and saves to database
     */
    @FXML
    private void handleConfirmDecision() {
        if (selectedDecisionIndex < 0) {
            showAlert("⚠ Please select a decision first", "#f39c12");
            return;
        }

        String decisionName = "";
        double costAmount = 0;
        double satisfactionChange = 0;
        String result = "";

        // Apply decision effects based on selected index
        switch (selectedDecisionIndex) {
            case 0: // Curfew
                decisionName = "Curfew";
                satisfactionChange = -20;
                result = (Math.random() < 0.5) ? "SUCCESS: Reduced crime by 50%" : "PARTIAL: Limited effectiveness";
                break;
            case 1: // Deploy Police
                decisionName = "Deploy Police";
                costAmount = 5000;
                if (city.getBudget() < costAmount) {
                    showAlert("❌ Not enough budget! Need $5,000", "#e74c3c");
                    return;
                }
                result = (Math.random() < 0.6) ? "SUCCESS: Police deployed effectively" : "PARTIAL: Limited resources";
                break;
            case 2: // Full Emergency
                decisionName = "Full Emergency";
                costAmount = 18000;
                if (city.getBudget() < costAmount) {
                    showAlert("❌ Not enough budget! Need $18,000", "#e74c3c");
                    return;
                }
                result = (Math.random() < 0.85) ? "SUCCESS: Full emergency response active" : "PARTIAL: Reduced capacity";
                break;
            case 3: // Wait & Monitor
                decisionName = "Wait & Monitor";
                result = "PASSIVE: Monitoring situation (15% recovery per day)";
                break;
        }

        // Apply budget change
        if (costAmount > 0) {
            city.setBudget(city.getBudget() - costAmount);
        }

        // Apply satisfaction change
        if (satisfactionChange != 0) {
            city.setSatisfaction(Math.max(0, Math.min(100, city.getSatisfaction() + satisfactionChange)));
        }

        // Save decision to database
        saveDecisionToDatabase(decisionName, costAmount, satisfactionChange, result);

        // Update UI
        updateStats();
        updateBuildingStatus();
        showAlert("✓ " + decisionName + ": " + result, "#27ae60");
        selectedDecisionIndex = -1;
        populateDecisions();
    }

    /**
     * Saves decision to database
     */
    private void saveDecisionToDatabase(String decisionName, double cost, double satisfactionChange, String result) {
        try {
            java.sql.Connection conn = com.govsim.govsim.database.DatabaseConnection.getConnection();
            String sql = "INSERT INTO decisions (user_id, decision_name, cost, satisfaction_change, result, day_taken) VALUES (?, ?, ?, ?, ?, ?)";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, decisionName);
            stmt.setDouble(3, cost);
            stmt.setDouble(4, satisfactionChange);
            stmt.setString(5, result);
            stmt.setInt(6, currentDay);
            stmt.executeUpdate();
            stmt.close();
            System.out.println("[Day " + currentDay + "] DECISION: " + decisionName + " - Cost: $" + cost + ", Satisfaction: " + satisfactionChange);
        } catch (Exception e) {
            System.err.println("Error saving decision: " + e.getMessage());
        }
    }

    /**
     * Handles logout
     */
    public void logout() {
        if (simulationTimer != null) {
            simulationTimer.cancel();
        }
        if (mainApp != null) {
            mainApp.logout();
        }
    }
}



