package com.govsim.govsim.simulation;

import com.govsim.govsim.ministry.Ministry;
import com.govsim.govsim.model.*;
import com.govsim.govsim.president.*;
import com.govsim.govsim.database.*;
import java.util.ArrayList;
import java.util.List;

/** GUI simulation engine — extends SimuEngine with GUI support */
public class SimuEngineGUI extends SimuEngine {

    // ─────────────────────────────────────────────────────
    // GUI LISTENER INTERFACE
    // ─────────────────────────────────────────────────────

    public interface SimulationListener {
        void onDayAdvanced(int day, int month, int year);
        void onDangerousEvent(Event event, DecisionOption[] options);
        void onDecisionApplied(DecisionOption chosen);
        void onGameOver(String reason);
        void onStatsUpdated();
        void onMonthEnd();
        void onMonthlyReport(List<Report> reports);
        void onAnnualReview(List<Minister> ministers, int year);
    }

    // ─────────────────────────────────────────────────────
    // GUI STATE
    // ─────────────────────────────────────────────────────

    private int              currentDay     = 1;
    private final int        daysInMonth    = 30;
    private Event            pendingEvent   = null;
    private DecisionOption[] currentOptions = null;
    private SimulationListener listener     = null;

    /**
     * Hard block: when true, advanceDayForGUI() does nothing.
     * Set to true before showing monthly/annual popup,
     * set back to false after popup is dismissed.
     */
    private volatile boolean blockAdvance      = false;
    private volatile int     pendingAnnualYear = -1; // >0 = annual review pending after monthly

    // Ministry health
    private int interiorHealth   = 100;
    private int defenseHealth    = 100;
    private int financeHealth    = 100;
    private int populationHealth = 100;
    private int healthHealth     = 100;

    // ─────────────────────────────────────────────────────
    // INIT FOR GUI
    // ─────────────────────────────────────────────────────

    public void initializeForGUI(int userId) {
        this.userId = userId;

        if (cityDAO.hasSave(userId)) {
            city = cityDAO.load(userId);
            System.out.println("[GUI Engine] Loaded saved city.");
        } else {
            city = new City(1_000_000);
            System.out.println("[GUI Engine] New game started.");
        }

        president = new President(city);
        setupMinistries();

        List<Minister> savedMinisters = ministerDAO.loadAll(userId);
        if (!savedMinisters.isEmpty()) loadMinisters(savedMinisters);

        generator.setMinistries(ministries);

        // Load current day from DB (restore exact position)
        if (cityDAO.hasSave(userId)) {
            currentDay = cityDAO.loadCurrentDay(userId);
        } else {
            currentDay = 1;
        }

        // Load ministry health from DB
        int[] h = cityDAO.loadMinistryHealth(userId);
        interiorHealth   = h[0];
        defenseHealth    = h[1];
        financeHealth    = h[2];
        populationHealth = h[3];
        healthHealth     = h[4];
    }

    public void setSimulationListener(SimulationListener listener) {
        this.listener = listener;
    }

    private void notify(Runnable action) {
        if (listener != null) action.run();
    }

    // ─────────────────────────────────────────────────────
    // ADVANCE ONE DAY — called by timer
    // ─────────────────────────────────────────────────────

    public void advanceDayForGUI() {
        if (blockAdvance) return;   // ← hard stop during popups

        currentDay++;
        if (currentDay > daysInMonth) {
            currentDay = 1;
            endOfMonth();           // sets blockAdvance=true before firing onMonthlyReport
            city.nextMonth();
            notify(() -> listener.onMonthEnd());

            // Annual review — store it to be fired AFTER monthly popup closes
            if (city.getMonth() == 1) {
                pendingAnnualYear = city.getYear() - 1;
            }
            return; // don't process events on month-change day
        }

        // Consequences — satisfaction is already adjusted inside DecisionImpact.trigger()
        // Route them and show in UI, but do NOT call handleDangerousEventForGUI again
        // (that would cause a double satisfaction deduction each escalation)
        List<Event> consequences = president.processDailyConsequences(currentDay);
        for (Event ce : consequences) {
            router.route(ce);
            reduceMinistryHealth(ce.getMinistry(), ce.getSeverity());
            pendingEvent   = ce;
            currentOptions = president.getEventOptions(ce);
            notify(() -> listener.onDangerousEvent(ce, currentOptions));
        }
        // Daily events
        List<Event> dailyEvents = generator.generateDailyEvents(currentDay);
        for (Event event : dailyEvents) {
            router.route(event);
            reduceMinistryHealth(event.getMinistry(), event.getSeverity());
            if (event.getSeverity() == Severity.DANGEROUS)
                handleDangerousEventForGUI(event);
        }

        recoverMinistryHealth();
        generatePassiveIncome();

        // Save current day position so player can resume from exact day
        cityDAO.save(userId, city,
                interiorHealth, defenseHealth, financeHealth,
                populationHealth, healthHealth, currentDay);

        notify(() -> listener.onStatsUpdated());
    }

    // ─────────────────────────────────────────────────────
    // DANGEROUS EVENT — GUI
    // ─────────────────────────────────────────────────────

    private void handleDangerousEventForGUI(Event event) {
        pendingEvent   = event;
        currentOptions = president.getEventOptions(event);
        System.out.println("[GUI Engine] Dangerous: " + event.getDescription());
        notify(() -> listener.onDangerousEvent(event, currentOptions));
    }

    // ─────────────────────────────────────────────────────
    // APPLY DECISION
    // ─────────────────────────────────────────────────────

    public void applyEventDecisionForGUI(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= currentOptions.length) return;

        DecisionOption chosen = currentOptions[optionIndex];
        Decision decision = president.applyEventDecision(
                pendingEvent, chosen, optionIndex, currentDay, router);

        eventDAO.saveAll(userId, List.of(pendingEvent));
        decisionDAO.save(userId, decision, city.getSatisfaction());

        System.out.println("[GUI Engine] Decision: " + chosen.title + " | Budget: " + city.getBudget());

        notify(() -> listener.onDecisionApplied(chosen));

        pendingEvent   = null;
        currentOptions = null;
    }

    // ─────────────────────────────────────────────────────
    // END OF MONTH — save everything
    // ─────────────────────────────────────────────────────

    private void endOfMonth() {
        city.applyMonthlyFinance();

        List<Report> reports = new ArrayList<>();
        for (int i = 0; i < ministries.size(); i++) {
            Report r = ministries.get(i).generateReport(city.getMonth(), city.getYear());
            reports.add(r);
            ministers.get(i).addMonthlyReport(r);
        }

        cityDAO.save(userId, city,
                interiorHealth, defenseHealth, financeHealth,
                populationHealth, healthHealth, 1); // day resets to 1 at start of new month
        ministerDAO.saveAll(userId, ministers);
        reportDAO.saveMonthly(userId, reports);

        for (Ministry m : ministries) {
            eventDAO.saveAll(userId, m.getEventLog());
            m.clearEventLog();
        }

        System.out.println("[GUI Engine] Month saved.");
        blockAdvance = true;   // pause engine until popup is dismissed
        notify(() -> listener.onMonthlyReport(reports));
    }

    // ─────────────────────────────────────────────────────
    // GAME OVER CHECK
    // ─────────────────────────────────────────────────────

    public boolean checkGameOverForGUI() {
        if (city.getSatisfaction() < 50) {
            notify(() -> listener.onGameOver("GAME OVER — People revolted! Satisfaction: " + (int) city.getSatisfaction() + "%"));
            return true;
        }
        if (city.getBudget() <= 0) {
            notify(() -> listener.onGameOver("GAME OVER — City bankrupt!"));
            return true;
        }
        if (city.getYear() > 1 && city.getMonth() > 1) {
            notify(() -> listener.onGameOver("YOU WIN — Survived 3 years!"));
            return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────

    private void generatePassiveIncome() {
        double avg   = (interiorHealth + defenseHealth + financeHealth
                + populationHealth + healthHealth) / 5.0;
        double bonus = (avg / 100.0) * 500;
        double mult  = city.getSatisfaction() / 100.0;
        city.setBudget(city.getBudget() + (5000 + bonus) * mult);
    }

    private void reduceMinistryHealth(String name, Severity severity) {
        // Only dangerous events reduce ministry health by 5
        if (severity != Severity.DANGEROUS) return;
        int r = 12;
        switch (name) {
            case "Interior"   -> interiorHealth   = Math.max(0, interiorHealth   - r);
            case "Defense"    -> defenseHealth    = Math.max(0, defenseHealth    - r);
            case "Finance"    -> financeHealth    = Math.max(0, financeHealth    - r);
            case "Population" -> populationHealth = Math.max(0, populationHealth - r);
            case "Health"     -> healthHealth     = Math.max(0, healthHealth     - r);
        }
    }

    /** +1 recovery per day per ministry if no dangerous event today */
    private void recoverMinistryHealth() {
        interiorHealth   = Math.min(100, interiorHealth   + 2);
        defenseHealth    = Math.min(100, defenseHealth    + 2);
        financeHealth    = Math.min(100, financeHealth    + 2);
        populationHealth = Math.min(100, populationHealth + 2);
        healthHealth     = Math.min(100, healthHealth     + 2);
    }

    // ─────────────────────────────────────────────────────
    // GETTERS
    // ─────────────────────────────────────────────────────

    public int getCurrentDay()                  { return currentDay; }
    public int getDaysInMonth()                 { return daysInMonth; }
    public DecisionOption[] getCurrentOptions() { return currentOptions; }
    public void setBlockAdvance(boolean block)  { this.blockAdvance = block; }

    /** Returns pending annual review year (>0) and resets it, or -1 if none */
    public int consumePendingAnnualYear() {
        int y = pendingAnnualYear;
        pendingAnnualYear = -1;
        return y;
    }

    public int getInteriorHealth()   { return interiorHealth; }
    public int getDefenseHealth()    { return defenseHealth; }
    public int getFinanceHealth()    { return financeHealth; }
    public int getPopulationHealth() { return populationHealth; }
    public int getHealthHealth()     { return healthHealth; }

    /** Delete ALL save data on game over/win — city cascade deletes ministers/events/reports */
    public void deleteSave() {
        eventDAO.deleteAll(userId);
        cityDAO.delete(userId); // FK cascade removes ministers, reports, decisions
        System.out.println("[GUI Engine] All save data deleted for user " + userId);
    }
}