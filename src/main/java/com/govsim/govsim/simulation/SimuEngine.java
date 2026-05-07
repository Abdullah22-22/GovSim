package com.govsim.govsim.simulation;

import com.govsim.govsim.ministry.*;
import com.govsim.govsim.model.*;
import com.govsim.govsim.president.*;
import com.govsim.govsim.database.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Main simulation engine — console game loop */
public class SimuEngine {

    protected final Scanner       scanner     = new Scanner(System.in);
    protected City                city;
    protected EventGenerator      generator;
    protected EventRouter         router;
    protected President           president;
    protected AnnualReview        annualReview;
    protected ReportGenerator     reportGen   = new ReportGenerator();
    protected List<Ministry>      ministries  = new ArrayList<>();
    protected List<Minister>      ministers   = new ArrayList<>();

    protected final UserManager   userManager = new UserManager();
    protected final CityDAO       cityDAO     = new CityDAO();
    protected final MinisterDAO   ministerDAO = new MinisterDAO();
    protected final EventDAO      eventDAO    = new EventDAO();
    protected final ReportDAO     reportDAO   = new ReportDAO();
    protected final DecisionDAO   decisionDAO = new DecisionDAO();
    protected int userId = -1;

    // ─────────────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────────────

    public SimuEngine() {
        this.generator = new EventGenerator();
        this.router    = new EventRouter();
    }

    // ─────────────────────────────────────────────────────
    // SETUP
    // ─────────────────────────────────────────────────────

    protected void setupMinistries() {
        ministries.clear();
        ministers.clear();
        registerMinistry(new InteriorMinistry(),   new Minister("Ahmed",    "Interior"));
        registerMinistry(new DefenseMinistry(),    new Minister("Hassan",   "Defense"));
        registerMinistry(new FinanceMinistry(),    new Minister("Indrek",   "Finance"));
        registerMinistry(new HealthMinistry(),     new Minister("Armin",    "Health"));
        registerMinistry(new PopulationMinistry(), new Minister("Abdullah", "Population"));
    }

    protected void registerMinistry(Ministry ministry, Minister minister) {
        ministries.add(ministry);
        ministers.add(minister);
        router.addMinistry(ministry.getName(), ministry);
    }

    public void loadMinisters(List<Minister> saved) {
        for (int i = 0; i < ministers.size() && i < saved.size(); i++) {
            ministers.get(i).setScore(saved.get(i).getScore());
            ministers.get(i).setWarnings(saved.get(i).getWarnings());
            ministers.get(i).setStatus(saved.get(i).getStatus());
        }
    }

    // ─────────────────────────────────────────────────────
    // MAIN CONSOLE LOOP
    // ─────────────────────────────────────────────────────

    public void start() {
        User user = userManager.authenticate(scanner);
        this.userId = user.getId();

        if (cityDAO.hasSave(userId)) {
            System.out.println("\nSaved game found! Loading...");
            this.city = cityDAO.load(userId);
            setupMinistries();
            generator.setMinistries(ministries);
            List<Minister> saved = ministerDAO.loadAll(userId);
            if (!saved.isEmpty()) loadMinisters(saved);
        } else {
            System.out.println("\nNo saved game. Starting new...");
            this.city = new City(1000000);
            setupMinistries();
            generator.setMinistries(ministries);
        }

        this.president    = new President(city);
        this.annualReview = new AnnualReview(president, scanner);

        for (int i = 0; i < 36; i++) {
            runMonth();
            cityDAO.save(userId, city);
            ministerDAO.saveAll(userId, ministers);

            if (city.getSatisfaction() < 50 || city.getBudget() <= 0) {
                System.out.println("GAME OVER — Deleting save...");
                cityDAO.delete(userId);
                break;
            }
            if (city.getYear() > 3) {
                System.out.println("YOU WIN — Deleting save...");
                cityDAO.delete(userId);
                break;
            }
        }

        DBManager.close();
    }

    // ─────────────────────────────────────────────────────
    // MONTH LOOP
    // ─────────────────────────────────────────────────────

    public void runMonth() {
        System.out.println("\n=== Month " + city.getMonth() + " / Year " + city.getYear() + " ===");

        for (int day = 1; day <= 30; day++) {
            List<Event> consequences = president.processDailyConsequences(day);
            for (Event ce : consequences) {
                handleDangerousEvent(ce, day);
                router.route(ce);
            }

            List<Event> events = generator.generateDailyEvents(day);
            for (Event event : events) {
                System.out.println(event);
                if (event.getSeverity() == Severity.DANGEROUS)
                    handleDangerousEvent(event, day);
                router.route(event);
            }
        }

        city.applyMonthlyFinance();

        List<Report> reports = new ArrayList<>();
        for (int i = 0; i < ministries.size(); i++) {
            Report r = ministries.get(i).generateReport(city.getMonth(), city.getYear());
            reports.add(r);
            ministers.get(i).addMonthlyReport(r);
        }

        for (Ministry m : ministries) {
            eventDAO.saveAll(userId, m.getEventLog());
            m.clearEventLog();
        }
        reportDAO.saveMonthly(userId, reports);
        reportGen.printMonthlyReport(city, reports);
        president.applyMonthlyDecisions(reports, scanner);
        city.nextMonth();

        if (city.getMonth() == 1)
            annualReview.run(ministers, city.getYear());

        checkGameOver();
    }

    // ─────────────────────────────────────────────────────
    // DANGEROUS EVENT — console
    // ─────────────────────────────────────────────────────

    protected void handleDangerousEvent(Event event, int day) {
        DecisionOption[] options = president.getEventOptions(event);

        System.out.println("\n========================================");
        System.out.println("DANGEROUS EVENT — " + event.getMinistry());
        System.out.println("Event: " + event.getDescription());
        System.out.printf("Budget: EUR %.0f | Satisfaction: %.0f%%%n",
                city.getBudget(), city.getSatisfaction());
        System.out.println("========================================");

        for (int i = 0; i < options.length; i++) {
            System.out.printf("  %d. %-20s - %-45s - Cost: EUR %d%n",
                    i + 1, options[i].title, options[i].description, options[i].cost);
        }

        int choice = -1;
        while (choice < 0 || choice > 2) {
            System.out.print("Choose (1, 2, or 3): ");
            try {
                choice = scanner.nextInt() - 1;
                if (choice < 0 || choice > 2)
                    System.out.println("Invalid!");
            } catch (Exception e) {
                System.out.println("Invalid!");
                scanner.nextLine();
            }
        }

        Decision decision = president.applyEventDecision(event, options[choice], choice, day, router);
        eventDAO.saveAll(userId, List.of(event));
        decisionDAO.save(userId, decision, city.getSatisfaction());
        System.out.println("Decision: " + decision.getChoice() + " | Budget left: EUR " + (int) city.getBudget());
    }

    // ─────────────────────────────────────────────────────
    // GAME OVER
    // ─────────────────────────────────────────────────────

    protected void checkGameOver() {
        if (city.getSatisfaction() < 50)
            System.out.println("GAME OVER — People revolted!");
        if (city.getBudget() <= 0)
            System.out.println("GAME OVER — City bankrupt!");
        if (city.getYear() > 3)
            System.out.println("YOU WIN!");
    }

    // ─────────────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────────────

    public City           getCity()       { return city; }
    public President      getPresident()  { return president; }
    public List<Ministry> getMinistries() { return ministries; }
    public List<Minister> getMinisters()  { return ministers; }
}