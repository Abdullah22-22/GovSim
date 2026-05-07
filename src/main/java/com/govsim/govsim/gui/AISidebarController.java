package com.govsim.govsim.gui;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.president.DecisionOption;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * AISidebarController — right sidebar.
 * Shows "monitoring" state when idle, or AI decision options
 * when a dangerous event occurs.
 * Notifies DashboardController when a decision is selected.
 */
public class AISidebarController {

    @FXML private VBox  contentContainer;
    @FXML private Label headerLabel;
    @FXML private Label headerIcon;

    private int                selectedIndex = -1;
    private OnDecisionSelected onDecision    = null;

    // ─────────────────────────────────────────────────────
    // CALLBACK INTERFACE
    // ─────────────────────────────────────────────────────

    /** DashboardController registers here to receive selection events */
    public interface OnDecisionSelected {
        void onSelected(int index);
    }

    public void setOnDecisionSelected(OnDecisionSelected callback) {
        this.onDecision = callback;
    }

    public int getSelectedIndex() { return selectedIndex; }

    // ─────────────────────────────────────────────────────
    // WAITING STATE — no active dangerous event
    // ─────────────────────────────────────────────────────

    public void showWaiting() {
        selectedIndex = -1;
        contentContainer.getChildren().clear();

        headerIcon.setText("🤖");
        headerLabel.setText("AI Advisor");
        headerLabel.getStyleClass().removeAll("text-red", "text-yellow");
        headerLabel.getStyleClass().add("text-white");

        VBox card = new VBox(10);
        card.getStyleClass().add("sidebar-wait-card");

        Label icon = new Label("👁");
        icon.setStyle("-fx-font-size: 28;");

        Label msg = new Label("Monitoring city...");
        msg.getStyleClass().addAll("text-muted", "text-bold");
        msg.setStyle("-fx-font-size: 11;");

        Label sub = new Label("AI advisor activates\nwhen a dangerous event\noccurs.");
        sub.getStyleClass().add("text-muted");
        sub.setStyle("-fx-font-size: 10; -fx-wrap-text: true;");

        card.getChildren().addAll(icon, msg, sub);
        contentContainer.getChildren().add(card);
    }

    // ─────────────────────────────────────────────────────
    // DANGER STATE — show AI decision options
    // ─────────────────────────────────────────────────────

    /**
     * Displays the dangerous event details and 3 AI decision options.
     *
     * @param event   the dangerous event
     * @param options AI-generated options from Groq
     */
    public void showDangerousEvent(Event event, DecisionOption[] options) {
        selectedIndex = -1;
        contentContainer.getChildren().clear();

        // Update header
        headerIcon.setText("⚠");
        headerLabel.setText("DANGEROUS EVENT");
        headerLabel.getStyleClass().removeAll("text-white", "text-yellow");
        headerLabel.getStyleClass().add("text-red");

        // Event summary card
        VBox eventCard = new VBox(6);
        eventCard.getStyleClass().add("sidebar-event-card");

        Label ministryLabel = new Label(icon(event.getMinistry()) + " " + event.getMinistry());
        ministryLabel.getStyleClass().addAll("text-red", "text-bold");
        ministryLabel.setStyle("-fx-font-size: 11;");

        Label descLabel = new Label(event.getDescription());
        descLabel.getStyleClass().add("text-white");
        descLabel.setStyle("-fx-font-size: 10; -fx-wrap-text: true;");
        descLabel.setWrapText(true);

        eventCard.getChildren().addAll(ministryLabel, descLabel);
        contentContainer.getChildren().add(eventCard);

        // AI label
        Label aiLabel = new Label("🤖 AI Suggestions:");
        aiLabel.getStyleClass().addAll("text-yellow", "text-bold");
        aiLabel.setStyle("-fx-font-size: 10; -fx-padding: 8 0 4 0;");
        contentContainer.getChildren().add(aiLabel);

        // Option cards
        VBox[] cards = new VBox[options.length];

        for (int i = 0; i < options.length; i++) {
            final int idx = i;
            DecisionOption opt = options[i];

            VBox card = new VBox(5);
            card.getStyleClass().add("sidebar-option");
            card.setCursor(Cursor.HAND);

            Label title = new Label((idx + 1) + ". " + opt.title);
            title.getStyleClass().addAll("text-bold",
                    idx == 0 ? "text-red" : idx == 1 ? "text-yellow" : "text-green");
            title.setStyle("-fx-font-size: 12;");

            Label desc = new Label(opt.description);
            desc.getStyleClass().add("text-muted");
            desc.setStyle("-fx-font-size: 9; -fx-wrap-text: true;");
            desc.setWrapText(true);

            Label cost = new Label("💰 Cost: €" + opt.cost);
            cost.getStyleClass().add("text-yellow");
            cost.setStyle("-fx-font-size: 9;");

            card.getChildren().addAll(title, desc, cost);
            cards[i] = card;

            // Selection handler
            card.setOnMouseClicked(e -> {
                selectedIndex = idx;
                for (int k = 0; k < cards.length; k++) {
                    cards[k].getStyleClass().removeAll(
                            "sidebar-option", "sidebar-option-selected");
                    cards[k].getStyleClass().add(
                            k == idx ? "sidebar-option-selected" : "sidebar-option");
                }
                if (onDecision != null) onDecision.onSelected(idx);
            });

            contentContainer.getChildren().add(card);
        }
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