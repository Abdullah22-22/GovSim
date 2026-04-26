package com.govsim.govsim.president;

import com.govsim.govsim.model.*;
import com.govsim.govsim.simulation.AIAdvisor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * President — makes all major decisions using AI suggestions:
 * 1. Dangerous event — picks from 3 AI DecisionOptions
 * 2. Monthly report — applies ADD/CUT/KEEP per ministry
 * 3. Annual review — keeps or fires ministers
 */
public class President {

    private final City city;
    private final AIAdvisor advisor;
    private final List<Decision> decisionHistory = new ArrayList<>();

    public President(City city) {
        this.city = city;
        this.advisor = new AIAdvisor();
    }

    // ─────────────────────────────────────────────────────
    // 1. DANGEROUS EVENT
    // AI returns 3 DecisionOptions — president picks 0, 1, or 2
    // Cost comes from Gemini based on city budget
    // ─────────────────────────────────────────────────────

    public DecisionOption[] getEventOptions(Event event) {
        // Get 3 AI options from Gemini
        return advisor.suggestForEvent(event, city);
    }

    public Decision applyEventDecision(Event event, DecisionOption chosen, int choiceIndex) {

        // Satisfaction changes based on choice index
        double satisfactionChange = switch (choiceIndex) {
            case 0 -> -5; // ignore
            case 1 -> -1; // partial
            case 2 -> +3; // full response
            default -> -2;
        };

        // Apply cost and satisfaction to city
        city.setBudget(city.getBudget() - chosen.cost);
        city.setSatisfaction(Math.max(0, Math.min(
                100, city.getSatisfaction() + satisfactionChange)));

        // Save to history
        Decision decision = new Decision(event, chosen.title, chosen.cost);
        decision.setOutcome(chosen.description);
        decisionHistory.add(decision);

        System.out.println("[President] Chosen: " + chosen);
        System.out.println("Budget: €" + city.getBudget() +
                " | Satisfaction: " + city.getSatisfaction() + "%");
        return decision;
    }

    // ─────────────────────────────────────────────────────
    // 2. MONTHLY REPORT
    // AI returns ADD/CUT/KEEP per ministry as DecisionOptions
    // President applies budget changes to city
    // ─────────────────────────────────────────────────────

    public DecisionOption[] getMonthlyOptions(List<Report> reports) {
        // Get AI budget suggestions for all ministries
        return advisor.suggestForMonthlyReport(reports, city);
    }

    public void applyMonthlyDecisions(List<Report> reports) {
        System.out.println("[President] Monthly Report Review:");

        // Print ministry performance
        for (Report r : reports) {
            System.out.println("  " + r.getMinistry() +
                    " | Rating: " + r.getRating() + "%" +
                    " | Resolved: " + r.getResolved() +
                    " | Ignored: " + r.getUnresolved());
        }

        // Get and apply AI budget suggestions
        DecisionOption[] suggestions = getMonthlyOptions(reports);
        System.out.println("[President] AI budget suggestions:");

        for (DecisionOption option : suggestions) {
            if (option == null)
                continue;
            System.out.println("  -> " + option);

            // Apply ADD or CUT to city budget
            switch (option.title.toUpperCase()) {

                case "ADD" -> {
                    city.setBudget(city.getBudget() - option.cost);
                    System.out.println("     Invested: -€" + option.cost);
                }
                case "CUT" -> {
                    city.setBudget(city.getBudget() + option.cost);
                    System.out.println("     Saved: +€" + option.cost);
                }
                case "KEEP" -> System.out.println("     No change.");
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // 3. ANNUAL REVIEW
    // AI returns KEEP or FIRE per minister
    // President applies the decision to each minister
    // ─────────────────────────────────────────────────────
    public void reviewMinistersAnnually(List<Minister> ministers, Map<String, Double> avgRatings) {

        System.out.println("[President] Annual Minister Review:");

        for (Minister minister : ministers) {

            double avg = avgRatings.getOrDefault(minister.getMinistry(), 100.0);

            // Get AI suggestion for this minister
            DecisionOption option = advisor.suggestForAnnualReview(minister, avg);

            System.out.println("  " + minister.getName() +
                    " | Avg: " + avg + "%" +
                    " | AI: " + option);

            // Apply KEEP or FIRE
            switch (option.title.toUpperCase()) {
                case "FIRE" -> {
                    minister.setStatus("FIRED");
                    System.out.println("  -> FIRED");
                }
                default -> {
                    minister.setStatus("KEPT");
                    System.out.println("  -> KEPT");
                }
            }
        }
    }

    public List<Decision> getDecisionHistory() {
        return decisionHistory;
    }

    public City getCity() {
        return city;
    }
}