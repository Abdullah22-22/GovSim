package com.govsim.govsim.model;

import java.util.ArrayList;
import java.util.List;

/** Monthly report for one ministry */
public class Report {

    private String ministry;
    private int month;
    private int year;
    private List<Event> events;
    private double rating;

    /** Creates a new Report */
    public Report(String ministry, int month, int year) {
        this.ministry = ministry;
        this.month = month;
        this.year = year;
        this.events = new ArrayList<>();
        this.rating = 100.0;
    }

    // Getters
    public String getMinistry() {
        return ministry;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public List<Event> getEvents() {
        return events;
    }

    public double getRating() {
        return rating;
    }

    // Setters
    public void addEvent(Event event) {
        events.add(event);
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotalEvents() {
        return events.size();
    }

    public int getResolved() {
        return (int) events.stream().filter(Event::isResolved).count();
    }

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
