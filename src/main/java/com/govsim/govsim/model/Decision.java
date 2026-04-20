package com.govsim.govsim.model;

/** President decision linked to a dangerous event */
public class Decision {

    private Event event;
    private String choice;
    private double cost;
    private String outcome;

    /** Creates a new Decision */
    public Decision(Event event, String choice, double cost) {
        this.event = event;
        this.choice = choice;
        this.cost = cost;
        this.outcome = "PENDING";
    }

    // Getters
    public Event getEvent() {
        return event;
    }

    public String getChoice() {
        return choice;
    }

    public double getCost() {
        return cost;
    }

    public String getOutcome() {
        return outcome;
    }

    // Setter
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    // Export
    @Override
    public String toString() {
        return "Decision: " + choice + " | Cost: €" + cost + " | Outcome: " + outcome;
    }
}