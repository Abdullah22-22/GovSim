package com.govsim.govsim.simulation;

import com.govsim.govsim.ministry.*;
import com.govsim.govsim.model.*;
import com.govsim.govsim.president.*;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

/** Main simulation engine — runs the full day loop */
public class SimuEngine {
    private final Scanner scanner = new Scanner(System.in);
    private City city;
    private EventGenerator generator;
    private EventRouter router;
    private President president;
    private List<Ministry> ministries = new ArrayList<>();

    /** Setup simulation */
    public SimuEngine(double startBudget) {
        this.city = new City(startBudget);
        this.generator = new EventGenerator();
        this.router = new EventRouter();
        this.president = new President(city);

        // Register all ministries — SP3
        // registerMinistry(new InteriorMinistry());
        registerMinistry(new DefenseMinistry());
        registerMinistry(new FinanceMinistry());
        // registerMinistry(new HealthMinistry());
        // registerMinistry(new PopulationMinistry());
    }

    /** Register ministry to both list and router */
    private void registerMinistry(Ministry ministry) {
        ministries.add(ministry);
        router.addMinistry(ministry.getName(), ministry);
    }

    /** Run one full month */
    public void runMonth() {
        System.out.println("\n=== Month " + city.getMonth() + " / Year " + city.getYear() + " ===");

        // Day loop
        for (int day = 1; day <= 30; day++) {
            List<Event> events = generator.generateDailyEvents(day);

            for (Event event : events) {
                System.out.println(event);

                // Dangerous event — AI suggests 3 options, player chooses
                if (event.getSeverity() == Severity.DANGEROUS) {
                    DecisionOption[] options = president.getEventOptions(event);

                    // Display event info and AI suggestions
                    System.out.println("\n========================================");
                    System.out.println("DANGEROUS EVENT — " + event.getMinistry() + " Ministry");
                    System.out.println("Event: " + event.getDescription());
                    System.out.printf("Budget: EUR %.0f | Satisfaction: %.0f%%%n",
                            city.getBudget(), city.getSatisfaction());
                    System.out.println("========================================");
                    System.out.println("AI Advisor suggests:");
                    for (int i = 0; i < options.length; i++) {
                        System.out.printf("  %d. %-20s - %-45s - Cost: EUR %d%n",
                                i + 1,
                                options[i].title,
                                options[i].description,
                                options[i].cost);
                    }
                    System.out.println("========================================");

                    // Wait for player input
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

                    Decision decision = president.applyEventDecision(event, options[choice], choice);
                    System.out.println("Decision: " + decision.getChoice() +
                            " | Cost: EUR " + (int) decision.getCost() +
                            " | Budget left: EUR " + (int) city.getBudget());
                    System.out.println();
                }

                router.route(event);
            }
        }

        // End of month
        city.applyMonthlyFinance();
        System.out.println("\n--- End of Month ---");
        System.out.println(city);
        city.nextMonth();

        // Check game over
        checkGameOver();
    }

    /** Check win / lose conditions */
    private void checkGameOver() {
        if (city.getSatisfaction() < 50) {
            System.out.println("GAME OVER — People revolted! Satisfaction: " + city.getSatisfaction() + "%");
        }
        if (city.getBudget() <= 0) {
            System.out.println("GAME OVER — City bankrupt! Budget: €" + city.getBudget());
        }
        if (city.getYear() > 3) {
            System.out.println("YOU WIN — Survived 3 years!");
        }
    }

    public City getCity() {
        return city;
    }

    public President getPresident() {
        return president;
    }

    public List<Ministry> getMinistries() {
        return ministries;
    }
}