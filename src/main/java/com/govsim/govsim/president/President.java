package com.govsim.govsim.president;

import com.govsim.govsim.model.*;
import com.govsim.govsim.simulation.AIAdvisor;
import com.govsim.govsim.simulation.EventRouter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * President — makes all major decisions using AI suggestions:
 * 1. Dangerous event  — picks from 3 AI DecisionOptions
 * 2. Monthly report   — applies ADD/CUT/KEEP per ministry
 * 3. Annual review    — keeps or fires ministers
 */
public class President {

    private final City           city;
    private final AIAdvisor      advisor;
    private final DecisionImpact impact;
    private final List<Decision> decisionHistory = new ArrayList<>();

    public President(City city) {
        this.city    = city;
        this.advisor = new AIAdvisor();
        this.impact  = new DecisionImpact();
    }

    // ─────────────────────────────────────────────────────
    // 1. DANGEROUS EVENT
    // AI returns 3 DecisionOptions — president picks 0, 1, or 2
    // Consequence scheduled after 3 days — not immediately
    // ─────────────────────────────────────────────────────

    public DecisionOption[] getEventOptions(Event event) {
        // Get 3 AI options from Groq
        return advisor.suggestForEvent(event, city);
    }

    public Decision applyEventDecision(Event event, DecisionOption chosen,
                                       int choiceIndex, int currentDay,
                                       EventRouter router) {
        // Apply cost to city budget immediately
        city.setBudget(city.getBudget() - chosen.cost);

        // Apply immediate satisfaction change
        double satisfactionChange = switch (choiceIndex) {
            case 0 -> -5;  // ignore
            case 1 -> -1;  // partial
            case 2 -> +3;  // full response
            default -> -2;
        };
        city.setSatisfaction(Math.max(0, Math.min(100,
                city.getSatisfaction() + satisfactionChange)));

        // Schedule consequence — triggers after 3 days
        impact.schedule(currentDay, event, choiceIndex);

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
    // Called every day — returns new consequence events
    // SimuEngine handles them as dangerous events
    // ─────────────────────────────────────────────────────

    public List<Event> processDailyConsequences(int currentDay) {
        return impact.processDailyConsequences(currentDay, city);
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

    public void applyMonthlyDecisions(List<Report> reports, Scanner scanner) {
        System.out.println("[President] Monthly Report Review:");

        for (Report r : reports) {
            System.out.println("\n========================================");
            System.out.println("MONTHLY REVIEW — " + r.getMinistry() + " Ministry");
            System.out.println("Rating: " + r.getRating() + "%" +
                    " | Resolved: " + r.getResolved() +
                    " | Ignored: "  + r.getUnresolved());
            System.out.println("========================================");

            // Get 3 AI options for this ministry
            DecisionOption[] options = advisor.suggestForMinistryReview(r, city);

            System.out.println("AI Advisor suggests:");
            for (int i = 0; i < options.length; i++) {
                System.out.printf("  %d. %-12s - %-40s - Cost: EUR %d%n",
                        i + 1,
                        options[i].title,
                        options[i].description,
                        options[i].cost);
            }
            System.out.println("========================================");

            // Player chooses
            int choice = -1;
            while (choice < 0 || choice > 2) {
                System.out.print("Choose option (1, 2, or 3): ");
                try {
                    choice = scanner.nextInt() - 1;
                    if (choice < 0 || choice > 2)
                        System.out.println("Invalid! Enter 1, 2, or 3.");
                } catch (Exception e) {
                    System.out.println("Invalid! Enter 1, 2, or 3.");
                    scanner.nextLine();
                }
            }

            // Apply chosen option
            DecisionOption chosen = options[choice];
            switch (chosen.title.toUpperCase()) {
                case "ADD" -> {
                    city.setBudget(city.getBudget() - chosen.cost);
                    System.out.println("  -> Invested: -EUR " + chosen.cost);
                }
                case "CUT" -> {
                    city.setBudget(city.getBudget() + chosen.cost);
                    System.out.println("  -> Saved: +EUR " + chosen.cost);
                }
                case "KEEP" -> System.out.println("  -> No change.");
            }
        }
    }
    // ─────────────────────────────────────────────────────
    // 3. ANNUAL REVIEW
    // AI returns KEEP or FIRE per minister
    // President applies the decision to each minister
    // ─────────────────────────────────────────────────────

    public void reviewMinistersAnnually(List<Minister> ministers,
                                        Map<String, Double> avgRatings) {
        System.out.println("[President] Annual Minister Review:");

        for (Minister minister : ministers) {
            double avg = avgRatings.getOrDefault(minister.getMinistry(), 100.0);

            // Get AI suggestion for this minister
            DecisionOption option = advisor.suggestForAnnualReview(minister, avg);

            System.out.println("  " + minister.getName() +
                    " | Avg: " + avg + "%" +
                    " | AI: "  + option);

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
    public DecisionOption getAnnualOption(Minister minister, double avgRating) {
        return advisor.suggestForAnnualReview(minister, avgRating);
    }

    public List<Decision> getDecisionHistory() { return decisionHistory; }
    public City           getCity()             { return city; }
}