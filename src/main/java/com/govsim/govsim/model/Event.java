package com.govsim.govsim.model;

/** One daily incident in the city */
public class Event {

    private String ministry;
    private String description;
    private Severity severity;
    private int day;
    private boolean resolved;

    /** Creates a new Event */
    public Event(String ministry, String description, Severity severity, int day) {
        this.ministry = ministry;
        this.description = description;
        this.severity = severity;
        this.day = day;
        this.resolved = false;
    }

    // Getters
    public String getMinistry() {
        return ministry;
    }

    public String getDescription() {
        return description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public int getDay() {
        return day;
    }

    public boolean isResolved() {
        return resolved;
    }

    // Setter
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    // Export
    @Override
    public String toString() {
        return "[Day " + day + "] " + ministry + " — " + description + " (" + severity + ")";
    }
}