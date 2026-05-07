package com.govsim.govsim.gui;

import com.govsim.govsim.model.City;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.president.DecisionOption;
import com.govsim.govsim.simulation.AIAdvisor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * MonthlyReportController — popup for end-of-month ministry review.
 * For each ministry, the player chooses ADD / KEEP / CUT from AI options.
 */
public class MonthlyReportController {

    @FXML private Label monthYearLabel;
    @FXML private VBox  ministriesContainer;

    private Stage        stage;
    private List<Report> reports;
    private City         city;
    private Runnable     onConfirmed;

    private final AIAdvisor    advisor = new AIAdvisor();
    private DecisionOption[][] allOptions;
    private int[]              selectedChoices;

    // ─────────────────────────────────────────────────────
    // INIT — called from DashboardController
    // ─────────────────────────────────────────────────────

    public void setData(Stage stage, List<Report> reports, City city, Runnable onConfirmed) {
        this.stage       = stage;
        this.reports     = reports;
        this.city        = city;
        this.onConfirmed = onConfirmed;

        allOptions      = new DecisionOption[reports.size()][];
        selectedChoices = new int[reports.size()];
        for (int i = 0; i < selectedChoices.length; i++) selectedChoices[i] = -1;

        monthYearLabel.setText(
                "MONTHLY REPORT — Month " + reports.get(0).getMonth()
                        + " / Year "              + reports.get(0).getYear());
        monthYearLabel.getStyleClass().add("popup-title");

        buildUI();
    }

    // ─────────────────────────────────────────────────────
    // BUILD UI
    // ─────────────────────────────────────────────────────

    private void buildUI() {
        ministriesContainer.getChildren().clear();

        for (int i = 0; i < reports.size(); i++) {
            Report r = reports.get(i);
            final int idx = i;

            DecisionOption[] options = advisor.suggestForMinistryReview(r, city);
            allOptions[i] = options;

            // ── Card ──
            VBox card = new VBox(8);
            card.getStyleClass().add("popup-card");

            // Header row
            HBox header = new HBox(10);
            Label nameLabel = new Label(icon(r.getMinistry()) + " " + r.getMinistry());
            nameLabel.getStyleClass().addAll("text-white", "text-bold");
            nameLabel.setStyle("-fx-font-size: 13;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label ratingLabel = new Label(String.format("Rating: %.0f%%", r.getRating()));
            ratingLabel.getStyleClass().add("text-bold");
            ratingLabel.getStyleClass().add(
                    r.getRating() >= 70 ? "text-green" :
                            r.getRating() >= 40 ? "text-yellow" : "text-red");
            ratingLabel.setStyle("-fx-font-size: 11;");

            header.getChildren().addAll(nameLabel, spacer, ratingLabel);

            // Stats row
            HBox stats = new HBox(20);
            Label eventsLbl   = new Label("Events: "   + r.getTotalEvents());
            Label resolvedLbl = new Label("Resolved: " + r.getResolved());
            Label ignoredLbl  = new Label("Ignored: "  + r.getUnresolved());
            eventsLbl.getStyleClass().addAll("text-muted", "text-small");
            resolvedLbl.getStyleClass().addAll("text-green", "text-small");
            ignoredLbl.getStyleClass().addAll("text-red", "text-small");
            stats.getChildren().addAll(eventsLbl, resolvedLbl, ignoredLbl);

            // Options row
            HBox optionsRow  = new HBox(8);
            VBox[] optCards  = new VBox[options.length];

            for (int j = 0; j < options.length; j++) {
                final int choiceIdx = j;
                DecisionOption opt = options[j];

                VBox optCard = new VBox(4);
                optCard.getStyleClass().add("popup-option");
                HBox.setHgrow(optCard, Priority.ALWAYS);

                Label titleLbl = new Label(opt.title);
                titleLbl.getStyleClass().add("text-bold");
                titleLbl.getStyleClass().add(
                        opt.title.equalsIgnoreCase("ADD")  ? "text-green" :
                                opt.title.equalsIgnoreCase("CUT")  ? "text-red"   :
                                        "text-blue");
                titleLbl.setStyle("-fx-font-size: 12;");

                Label descLbl = new Label(opt.description);
                descLbl.getStyleClass().addAll("text-muted", "text-small");
                descLbl.setStyle("-fx-wrap-text: true;");

                Label costLbl = new Label(opt.cost > 0 ? "€" + opt.cost : "No cost");
                costLbl.getStyleClass().addAll("text-yellow", "text-small");

                optCard.getChildren().addAll(titleLbl, descLbl, costLbl);
                optCards[j] = optCard;

                optCard.setOnMouseClicked(e -> {
                    selectedChoices[idx] = choiceIdx;
                    for (int k = 0; k < optCards.length; k++) {
                        optCards[k].getStyleClass().removeAll(
                                "popup-option", "popup-option-selected-blue");
                        optCards[k].getStyleClass().add(
                                k == choiceIdx
                                        ? "popup-option-selected-blue"
                                        : "popup-option");
                    }
                });

                optionsRow.getChildren().add(optCard);
            }

            card.getChildren().addAll(header, stats, optionsRow);
            ministriesContainer.getChildren().add(card);
        }
    }

    // ─────────────────────────────────────────────────────
    // CONFIRM
    // ─────────────────────────────────────────────────────

    @FXML
    private void handleConfirm() {
        // Validate all selected
        for (int i = 0; i < selectedChoices.length; i++) {
            if (selectedChoices[i] == -1) {
                monthYearLabel.setText("Please select a decision for all ministries!");
                monthYearLabel.getStyleClass().removeAll("popup-title");
                monthYearLabel.getStyleClass().addAll("text-red", "text-bold");
                return;
            }
        }

        // Apply decisions
        for (int i = 0; i < reports.size(); i++) {
            DecisionOption chosen = allOptions[i][selectedChoices[i]];
            switch (chosen.title.toUpperCase()) {
                case "ADD"  -> city.setBudget(city.getBudget() - chosen.cost);
                case "CUT"  -> city.setBudget(city.getBudget() + chosen.cost);
                case "KEEP" -> {}
            }
            System.out.println("[Monthly] " + reports.get(i).getMinistry()
                    + " -> " + chosen.title + " | Budget: " + city.getBudget());
        }

        stage.close();
        if (onConfirmed != null) onConfirmed.run();
    }

    // ─────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────

    private String icon(String ministry) {
        return switch (ministry) {
            case "Interior"   -> "🏠";
            case "Defense"    -> "⚔";
            case "Finance"    -> "💰";
            case "Health"     -> "🏥";
            case "Population" -> "👥";
            default           -> "•";
        };
    }
}