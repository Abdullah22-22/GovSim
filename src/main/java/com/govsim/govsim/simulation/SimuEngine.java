package com.govsim.govsim.simulation;

import com.govsim.govsim.ministry.*;
import com.govsim.govsim.model.*;
import com.govsim.govsim.president.*;
import com.govsim.govsim.database.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Main simulation engine — runs the full game loop */
public class SimuEngine {

    private final Scanner         scanner     = new Scanner(System.in);
    private City                  city;
    private EventGenerator        generator;
    private EventRouter           router;
    private President             president;
    private AnnualReview          annualReview;
    private ReportGenerator       reportGen   = new ReportGenerator();
    private List<Ministry>        ministries  = new ArrayList<>();
    private List<Minister>        ministers    = new ArrayList<>();

    private final UserManager     userManager = new UserManager();
    private final CityDAO         cityDAO     = new CityDAO();
    private final MinisterDAO     ministerDAO = new MinisterDAO();
    private final EventDAO        eventDAO    = new EventDAO();
    private final ReportDAO       reportDAO   = new ReportDAO();
    private final DecisionDAO     decisionDAO = new DecisionDAO();
    private int userId = -1;

    // ─────────────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────────────

    /** Start new game */
    public SimuEngine(double startBudget) {
        this.city         = new City(startBudget);
        this.generator    = new EventGenerator();
        this.router       = new EventRouter();
        this.president    = new President(city);
        this.annualReview = new AnnualReview(president, scanner);

        registerMinistry(new InteriorMinistry(),   new Minister("Ahmed",    "Interior"));
        registerMinistry(new DefenseMinistry(),    new Minister("Hassan",   "Defense"));
        registerMinistry(new FinanceMinistry(),    new Minister("Indrek",   "Finance"));
        registerMinistry(new HealthMinistry(),     new Minister("Armin",    "Health"));
        registerMinistry(new PopulationMinistry(), new Minister("Abdullah", "Population"));
    }

    /** Load from saved city */
    public SimuEngine(City savedCity) {
        this.city         = savedCity;
        this.generator    = new EventGenerator();
        this.router       = new EventRouter();
        this.president    = new President(city);
        this.annualReview = new AnnualReview(president, scanner);

        registerMinistry(new InteriorMinistry(),   new Minister("Ahmed",    "Interior"));
        registerMinistry(new DefenseMinistry(),    new Minister("Hassan",   "Defense"));
        registerMinistry(new FinanceMinistry(),    new Minister("Indrek",   "Finance"));
        registerMinistry(new HealthMinistry(),     new Minister("Armin",    "Health"));
        registerMinistry(new PopulationMinistry(), new Minister("Abdullah", "Population"));
    }

    /** No-arg constructor — used with start() */
    public SimuEngine() {
        this.generator = new EventGenerator();
        this.router    = new EventRouter();
    }

    // ─────────────────────────────────────────────────────
    // MAIN GAME LOOP — login, load/new, play, save
    // ─────────────────────────────────────────────────────

    public void start() {
        // Login or register
        User user = userManager.authenticate(scanner);
        this.userId = user.getId();

        // Load saved game or start new
        if (cityDAO.hasSave(userId)) {
            System.out.println("\nSaved game found! Loading...");
            this.city = cityDAO.load(userId);
            setupMinistries();
            generator.setMinistries(ministries);

            List<Minister> saved = ministerDAO.loadAll(userId);
            if (!saved.isEmpty()) {
                loadMinisters(saved);
            }
        } else {
            System.out.println("\nNo saved game. Starting new...");
            this.city = new City(1000000);
            setupMinistries();
            generator.setMinistries(ministries);
        }

        this.president    = new President(city);
        this.annualReview = new AnnualReview(president, scanner);

        // Run up to 36 months — 3 full years
        for (int i = 0; i < 36; i++) {
            runMonth();

            // Save after every month
            cityDAO.save(userId, city);
            ministerDAO.saveAll(userId, ministers);

            // Check lose
            if (city.getSatisfaction() < 50 || city.getBudget() <= 0) {
                System.out.println("GAME OVER — Deleting save...");
                cityDAO.delete(userId);
                break;
            }

            // Check win
            if (city.getYear() > 3) {
                System.out.println("YOU WIN — Deleting save...");
                cityDAO.delete(userId);
                break;
            }
        }

        DBManager.close();
    }

    // ─────────────────────────────────────────────────────
    // SETUP — register all ministries
    // ─────────────────────────────────────────────────────

    private void setupMinistries() {
        ministries.clear();
        ministers.clear();
        registerMinistry(new InteriorMinistry(),   new Minister("Ahmed",    "Interior"));
        registerMinistry(new DefenseMinistry(),    new Minister("Hassan",   "Defense"));
        registerMinistry(new FinanceMinistry(),    new Minister("Indrek",   "Finance"));
        registerMinistry(new HealthMinistry(),     new Minister("Armin",    "Health"));
        registerMinistry(new PopulationMinistry(), new Minister("Abdullah", "Population"));
    }

    /** Register ministry and minister */
    private void registerMinistry(Ministry ministry, Minister minister) {
        ministries.add(ministry);
        ministers.add(minister);
        router.addMinistry(ministry.getName(), ministry);
    }

    /** Load saved minister data */
    public void loadMinisters(List<Minister> saved) {
        for (int i = 0; i < ministers.size() && i < saved.size(); i++) {
            ministers.get(i).setScore(saved.get(i).getScore());
            ministers.get(i).setWarnings(saved.get(i).getWarnings());
            ministers.get(i).setStatus(saved.get(i).getStatus());
        }
    }

    // ─────────────────────────────────────────────────────
    // RUN ONE FULL MONTH
    // ─────────────────────────────────────────────────────

    public void runMonth() {
        System.out.println("\n=== Month " + city.getMonth() + " / Year " + city.getYear() + " ===");

        // Day loop
        for (int day = 1; day <= 30; day++) {

            // Process delayed consequences
            List<Event> consequenceEvents = president.processDailyConsequences(day);
            for (Event ce : consequenceEvents) {
                handleDangerousEvent(ce, day);
                router.route(ce);
            }

            // Generate daily events
            List<Event> events = generator.generateDailyEvents(day);

            for (Event event : events) {
                System.out.println(event);

                if (event.getSeverity() == Severity.DANGEROUS) {
                    handleDangerousEvent(event, day);
                }

                router.route(event);
            }
        }

        // End of month finances
        city.applyMonthlyFinance();

        // Collect reports from all ministries
        List<Report> reports = new ArrayList<>();
        for (int i = 0; i < ministries.size(); i++) {
            Report r = ministries.get(i).generateReport(city.getMonth(), city.getYear());
            reports.add(r);
            ministers.get(i).addMonthlyReport(r);
        }

        // Save events and reports to DB
        for (Ministry m : ministries) {
            List<Event> normalEvents = new ArrayList<>();
            for (Event e : m.getEventLog()) {
                if (e.getSeverity() == Severity.NORMAL) {
                    normalEvents.add(e);
                }
            }
            eventDAO.saveAll(userId, normalEvents);
        }
        reportDAO.saveMonthly(userId, reports);

        // Clear event logs after saving
        for (Ministry m : ministries) {
            m.clearEventLog();
        }

        // Print monthly report
        reportGen.printMonthlyReport(city, reports);

        // President reviews monthly reports
        president.applyMonthlyDecisions(reports, scanner);

        city.nextMonth();

        // Annual review — every 12 months
        if (city.getMonth() == 1) {
            annualReview.run(ministers, city.getYear());
        }

        // Check game over
        checkGameOver();
    }

    // ─────────────────────────────────────────────────────
    // DANGEROUS EVENT — show options and get player choice
    // ─────────────────────────────────────────────────────

    private void handleDangerousEvent(Event event, int day) {
        DecisionOption[] options = president.getEventOptions(event);

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

        Decision decision = president.applyEventDecision(
                event, options[choice], choice, day, router);

        // Save decision to DB
        eventDAO.saveAll(userId, List.of(event));
        decisionDAO.save(userId, decision, city.getSatisfaction());

        System.out.println("Decision: " + decision.getChoice() +
                " | Cost: EUR " + (int) decision.getCost() +
                " | Budget left: EUR " + (int) city.getBudget());
        System.out.println();
    }

    // ─────────────────────────────────────────────────────
    // GAME OVER CHECK
    // ─────────────────────────────────────────────────────

    private void checkGameOver() {
        if (city.getSatisfaction() < 50)
            System.out.println("GAME OVER — People revolted! Satisfaction: " + city.getSatisfaction() + "%");
        if (city.getBudget() <= 0)
            System.out.println("GAME OVER — City bankrupt! Budget: €" + city.getBudget());
        if (city.getYear() > 3)
            System.out.println("YOU WIN — Survived 3 years!");
    }

    // ─────────────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────────────

    public City           getCity()       { return city; }
    public President      getPresident()  { return president; }
    public List<Ministry> getMinistries() { return ministries; }
    public List<Minister> getMinisters()  { return ministers; }
}