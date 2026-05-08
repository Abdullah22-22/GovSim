package com.govsim.govsim.model;

/**
 * Represents a single daily incident in the city.
 * Each event belongs to one ministry and has a severity level.
 */

public class Event {

    /** The ministry responsible for this event. */
    private String ministry;
    /** Short description of the incident. */
    private String description;
    /** Severity level: NORMAL or DANGEROUS. */
    private Severity severity;
    /** The simulation day this event occurred. */
    private int day;
    /** Whether the event has been resolved. */
    private boolean resolved;

     /**
     * Creates a new Event.
     * @param ministry    the responsible ministry name
     * @param description short description of the incident
     * @param severity    NORMAL or DANGEROUS
     * @param day         the simulation day
     */
    public Event(String ministry, String description, Severity severity, int day) {
        this.ministry = ministry;
        this.description = description;
        this.severity = severity;
        this.day = day;
        this.resolved = false;
    }

    // Getters

    /**
     * Returns the ministry responsible for this event.
     * @return ministry name
     */
    public String getMinistry() {
        return ministry;
    }


    /**
     * Returns the event description.
     * @return description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the severity level of this event.
     * @return NORMAL or DANGEROUS
     */    public Severity getSeverity() {
        return severity;
    }
    /**
     * Returns the day this event occurred.
     * @return simulation day number
     */
    public int getDay() {
        return day;
    }
    /**
     * Returns whether this event has been resolved.
     * @return true if resolved, false otherwise
     */
    public boolean isResolved() {
        return resolved;
    }

    // Setter
    /**
     * Sets the resolved status of this event.
     * @param resolved true to mark as resolved
     */
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    // Export
    @Override
    public String toString() {
        return "[Day " + day + "] " + ministry + " — " + description + " (" + severity + ")";
    }
}