package com.govsim.govsim.gui;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Severity;
import com.govsim.govsim.ministry.Ministry;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * EventsSidebarController — left sidebar.
 * Shows all daily events from every ministry.
 * Called from DashboardController after each day advances.
 */
public class EventsSidebarController {

    @FXML private VBox  eventsContainer;
    @FXML private Label eventCountLabel;

    // ─────────────────────────────────────────────────────
    // UPDATE — called from DashboardController each day
    // ─────────────────────────────────────────────────────

    /**
     * Refreshes the event list from all ministry event logs.
     *
     * @param ministries all active ministries
     */
    public void update(List<Ministry> ministries) {
        eventsContainer.getChildren().clear();

        int total = 0;

        for (Ministry m : ministries) {
            for (Event event : m.getEventLog()) {
                eventsContainer.getChildren().add(buildRow(event, m.getName()));
                total++;
            }
        }

        // Update count label
        eventCountLabel.setText(total + " event" + (total != 1 ? "s" : "") + " today");
        eventCountLabel.getStyleClass().removeAll("text-green", "text-yellow", "text-red");
        eventCountLabel.getStyleClass().add(
                total == 0  ? "text-green"  :
                        total < 5   ? "text-yellow" :
                                "text-red");

        // Empty state
        if (total == 0) {
            Label empty = new Label("✓ No incidents today");
            empty.getStyleClass().add("incident-empty");
            eventsContainer.getChildren().add(empty);
        }
    }

    // ─────────────────────────────────────────────────────
    // BUILD EVENT ROW
    // ─────────────────────────────────────────────────────

    private HBox buildRow(Event event, String ministryName) {
        boolean danger = event.getSeverity() == Severity.DANGEROUS;

        HBox row = new HBox(8);
        row.getStyleClass().addAll(
                "incident-row",
                danger ? "incident-row-danger" : "incident-row-normal");

        // Severity dot
        Label dot = new Label(danger ? "!" : "•");
        dot.getStyleClass().add(danger ? "incident-dot-danger" : "incident-dot-normal");

        // Info column
        VBox info = new VBox(2);

        Label ministry = new Label(icon(ministryName) + " " + ministryName);
        ministry.getStyleClass().add(
                danger ? "incident-text-danger" : "sidebar-ministry-tag");
        ministry.setStyle("-fx-font-size: 9; -fx-font-weight: bold;");

        Label desc = new Label(event.getDescription());
        desc.getStyleClass().add(
                danger ? "incident-text-danger" : "incident-text-normal");
        desc.setWrapText(true);
        desc.setMaxWidth(200);

        info.getChildren().addAll(ministry, desc);
        row.getChildren().addAll(dot, info);
        return row;
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