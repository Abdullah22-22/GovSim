package com.govsim.govsim.model;

/**
 * Represents a president's decision linked to a dangerous event.
 * Stores the chosen action, its cost, and the resulting outcome.
 */public class Decision {

    /** The dangerous event this decision responds to. */
    private Event event;
    /** The chosen action title (e.g. DEPLOY, IGNORE). */
    private String choice;
    /** The financial cost of this decision in euros. */
    private double cost;
    /** The outcome of this decision (default: PENDING). */
    private String outcome;

    /**
     * Creates a new Decision for a dangerous event.
     * @param event  the event being decided on
     * @param choice the chosen action
     * @param cost   the cost in euros
     */
    public Decision(Event event, String choice, double cost) {
        this.event = event;
        this.choice = choice;
        this.cost = cost;
        this.outcome = "PENDING";
    }

    // Getters
    /**
     * Returns the event this decision is linked to.
     * @return the Event object
     */
    public Event getEvent() {
        return event;
    }
    /**
     * Returns the chosen action.
     * @return choice string
     */
    public String getChoice() {
        return choice;
    }
    /**
     * Returns the financial cost of this decision.
     * @return cost in euros
     */
    public double getCost() {
        return cost;
    }
    /**
     * Returns the outcome of this decision.
     * @return outcome string (PENDING by default)
     */
    public String getOutcome() {
        return outcome;
    }

    // Setter
    /**
     * Sets the outcome of this decision.
     * @param outcome the result string
     */
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    // Export
    @Override
    public String toString() {
        return "Decision: " + choice + " | Cost: €" + cost + " | Outcome: " + outcome;
    }
}