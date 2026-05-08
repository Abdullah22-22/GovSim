package com.govsim.govsim.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Monthly performance report for one ministry.
 * Tracks all events, resolved count, and overall rating.
 */
public class Report {

    /** The ministry this report belongs to. */
    private String ministry;
    /** The month this report covers (1-12). */
    private int month;
    /** The year this report covers. */
    private int year;
    /** All events that occurred this month in this ministry. */
    private List<Event> events;
    /** Overall performance rating (0.0 - 100.0). */
    private double rating;

    /**
     * Creates a new Report for the given ministry, month, and year.
     * @param ministry the ministry name
     * @param month    the report month (1-12)
     * @param year     the report year
     */
    public Report(String ministry, int month, int year) {
        this.ministry = ministry;
        this.month = month;
        this.year = year;
        this.events = new ArrayList<>();
        this.rating = 100.0;
    }

    // Getters
    /**
     * Returns the ministry name.
     * @return ministry string
     */
    public String getMinistry() {
        return ministry;
    }

    /**
     * Returns the month this report covers.
     * @return month (1-12)
     */
    public int getMonth() {
        return month;
    }
    /**
     * Returns the year this report covers.
     * @return year number
     */
    public int getYear() {
        return year;
    }
    /**
     * Returns all events recorded in this report.
     * @return list of Event objects
     */
    public List<Event> getEvents() {
        return events;
    }
    /**
     * Returns the overall performance rating.
     * @return rating (0.0 - 100.0)
     */
    public double getRating() {
        return rating;
    }

    // Setters
    /**
     * Adds an event to this report.
     * @param event the Event to add
     */
    public void addEvent(Event event) {
        events.add(event);
    }
    /**
     * Sets the overall performance rating.
     * @param rating new rating value (0.0 - 100.0)
     */
    public void setRating(double rating) {
        this.rating = rating;
    }
    /**
     * Returns the total number of events this month.
     * @return total event count
     */
    public int getTotalEvents() {
        return events.size();
    }
    /**
     * Returns the number of resolved events.
     * @return resolved count
     */
    public int getResolved() {
        return (int) events.stream().filter(Event::isResolved).count();
    }
    /**
     * Returns the number of unresolved events.
     * @return unresolved count
     */
    public int getUnresolved() {
        return getTotalEvents() - getResolved();
    }

    // Export
    @Override
    public String toString() {
        return "Report [" + ministry + "] Month:" + month +
                " Events:" + getTotalEvents() +
                " Resolved:" + getResolved() +
                " Rating:" + rating + "%";
    }
}
