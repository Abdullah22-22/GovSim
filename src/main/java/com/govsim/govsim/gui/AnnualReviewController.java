package com.govsim.govsim.gui;

import com.govsim.govsim.model.City;
import com.govsim.govsim.model.Minister;
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
 * AnnualReviewController — popup for end-of-year minister review.
 * For each minister, the player chooses KEEP or FIRE.
 */
public class AnnualReviewController {

    @FXML private Label yearLabel;
    @FXML private VBox  ministersContainer;

    private Stage          stage;
    private List<Minister> ministers;
    private City           city;
    private Runnable       onConfirmed;

    private final AIAdvisor advisor = new AIAdvisor();
    private int[]           selectedChoices;

    // ─────────────────────────────────────────────────────
    // INIT — called from DashboardController
    // ─────────────────────────────────────────────────────

    public void setData(Stage stage, List<Minister> ministers,
                        City city, int year, Runnable onConfirmed) {
        this.stage       = stage;
        this.ministers   = ministers;
        this.city        = city;
        this.onConfirmed = onConfirmed;

        selectedChoices = new int[ministers.size()];
        for (int i = 0; i < selectedChoices.length; i++) selectedChoices[i] = -1;

        yearLabel.setText("ANNUAL MINISTER REVIEW — Year " + year);
        yearLabel.getStyleClass().add("popup-title-annual");

        buildUI();
    }

    // ─────────────────────────────────────────────────────
    // BUILD UI
    // ─────────────────────────────────────────────────────

    private void buildUI() {
        ministersContainer.getChildren().clear();

        for (int i = 0; i < ministers.size(); i++) {
            Minister minister = ministers.get(i);
            final int idx = i;

            // Calculate average rating
            double totalRating = 0;
            for (Report r : minister.getMonthlyReports()) totalRating += r.getRating();
            double avgRating = minister.getMonthlyReports().isEmpty()
                    ? 100.0 : totalRating / minister.getMonthlyReports().size();

            // AI suggestion
            DecisionOption aiSuggestion = advisor.suggestForAnnualReview(minister, avgRating);

            // ── Minister card ──
            VBox card = new VBox(10);
            card.getStyleClass().add("popup-card");

            // Header
            HBox header = new HBox(10);
            Label nameLabel = new Label("👤 " + minister.getName());
            nameLabel.getStyleClass().addAll("text-white", "text-bold");
            nameLabel.setStyle("-fx-font-size: 13;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label ministryLabel = new Label(minister.getMinistry());
            ministryLabel.getStyleClass().addAll("text-blue", "text-bold");
            ministryLabel.setStyle("-fx-font-size: 11;");

            header.getChildren().addAll(nameLabel, spacer, ministryLabel);

            // Stats
            HBox stats = new HBox(20);

            Label avgLabel = new Label(String.format("Avg Rating: %.0f%%", avgRating));
            avgLabel.getStyleClass().add("text-bold");
            avgLabel.getStyleClass().add(
                    avgRating >= 70 ? "text-green" :
                            avgRating >= 40 ? "text-yellow" : "text-red");
            avgLabel.setStyle("-fx-font-size: 11;");

            Label warningsLabel = new Label("Warnings: " + minister.getWarnings());
            warningsLabel.getStyleClass().addAll("text-yellow", "text-small");

            Label reportsLabel = new Label("Reports: " + minister.getMonthlyReports().size());
            reportsLabel.getStyleClass().addAll("text-muted", "text-small");

            stats.getChildren().addAll(avgLabel, warningsLabel, reportsLabel);

            // AI hint
            Label aiHint = new Label(
                    "AI suggests: " + aiSuggestion.title + " — " + aiSuggestion.description);
            aiHint.getStyleClass().addAll("text-muted", "text-small");
            aiHint.setStyle("-fx-font-style: italic; -fx-wrap-text: true;");

            // ── KEEP / FIRE option cards ──
            HBox optionsRow = new HBox(10);

            VBox keepCard = buildDecisionCard("KEEP", "Minister continues in office", "text-green");
            VBox fireCard = buildDecisionCard("FIRE", "Remove minister from office",  "text-red");

            HBox.setHgrow(keepCard, Priority.ALWAYS);
            HBox.setHgrow(fireCard, Priority.ALWAYS);
            optionsRow.getChildren().addAll(keepCard, fireCard);

            // Click handlers
            keepCard.setOnMouseClicked(e -> {
                selectedChoices[idx] = 0;
                keepCard.getStyleClass().removeAll("popup-option", "popup-option-selected-red");
                keepCard.getStyleClass().add("popup-option-selected-green");
                fireCard.getStyleClass().removeAll("popup-option-selected-green", "popup-option-selected-red");
                fireCard.getStyleClass().add("popup-option");
            });

            fireCard.setOnMouseClicked(e -> {
                selectedChoices[idx] = 1;
                fireCard.getStyleClass().removeAll("popup-option", "popup-option-selected-green");
                fireCard.getStyleClass().add("popup-option-selected-red");
                keepCard.getStyleClass().removeAll("popup-option-selected-green", "popup-option-selected-red");
                keepCard.getStyleClass().add("popup-option");
            });

            card.getChildren().addAll(header, stats, aiHint, optionsRow);
            ministersContainer.getChildren().add(card);
        }
    }

    /** Builds a KEEP or FIRE clickable option card */
    private VBox buildDecisionCard(String title, String desc, String colorClass) {
        VBox card = new VBox(4);
        card.getStyleClass().add("popup-option");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll(colorClass, "text-bold");
        titleLabel.setStyle("-fx-font-size: 13;");

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().addAll("text-muted", "text-small");

        card.getChildren().addAll(titleLabel, descLabel);
        return card;
    }

    // ─────────────────────────────────────────────────────
    // CONFIRM
    // ─────────────────────────────────────────────────────

    @FXML
    private void handleConfirm() {
        // Validate all selected
        for (int i = 0; i < selectedChoices.length; i++) {
            if (selectedChoices[i] == -1) {
                yearLabel.setText("Please decide for all ministers!");
                yearLabel.getStyleClass().removeAll("popup-title-annual");
                yearLabel.getStyleClass().addAll("text-red", "text-bold");
                return;
            }
        }

        // Apply decisions
        for (int i = 0; i < ministers.size(); i++) {
            if (selectedChoices[i] == 0) {
                ministers.get(i).setStatus("ACTIVE");
                System.out.println("[Annual] KEPT: " + ministers.get(i).getName());
            } else {
                ministers.get(i).setStatus("FIRED");
                System.out.println("[Annual] FIRED: " + ministers.get(i).getName());
            }
        }

        stage.close();
        if (onConfirmed != null) onConfirmed.run();
    }
}