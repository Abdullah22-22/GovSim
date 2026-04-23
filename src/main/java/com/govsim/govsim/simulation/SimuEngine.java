package com.govsim.govsim.simulation;

import com.govsim.govsim.ministry.*;
import com.govsim.govsim.model.*;
import java.util.List;

/** Main simulation engine — runs the full day loop */
public class SimuEngine {

    private City city;
    private EventGenerator generator;
    private EventRouter router;

    /** Setup simulation */
    public SimuEngine(double startBudget) {

        this.city = new City(startBudget);
        this.generator = new EventGenerator();
        this.router = new EventRouter();

        // Register all ministries — SP3
        // router.addMinistry("Interior", new InteriorMinistry());
        // router.addMinistry("Defense", new DefenseMinistry());
        router.addMinistry("Finance", new FinanceMinistry());
        // router.addMinistry("Population", new PopulationMinistry());
        // router.addMinistry("Health", new HealthMinistry());
    }

    /** Run one full month */
    public void runMonth() {

        System.out.println("\n=== Month " + city.getMonth() + " / Year " + city.getYear() + " ===");

        // Day loop
        for (int day = 1; day <= 30; day++) {
            List<Event> events = generator.generateDailyEvents(day);

            for (Event event : events) {
                System.out.println(event);
                router.route(event);
            }
        }

        // End of month
        System.out.println("\n--- End of Month ---");
        city.nextMonth();

        // Check game over
        checkGameOver();
    }

    /** Check win / lose conditions */
    private void checkGameOver() {
        if (city.getSatisfaction() < 50) {
            System.out.println("GAME OVER — People revolted!");
        }
        if (city.getBudget() <= 0) {
            System.out.println("GAME OVER — City bankrupt!");
        }
        if (city.getYear() > 3) {
            System.out.println("YOU WIN — Survived 3 years!");
        }
    }

    public City getCity() {
        return city;
    }

}