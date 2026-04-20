package com.govsim.govsim.model;

/** Current state of the city */
public class City {

    private double budget;
    private double satisfaction;
    private int month;
    private int year;

    /** Creates a new City */
    public City(double budget) {
        this.budget = budget;
        this.satisfaction = 100.0;
        this.month = 1;
        this.year = 1;
    }

    // Getters
    public double getBudget() {
        return budget;
    }

    public double getSatisfaction() {
        return satisfaction;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    // Setters
    public void setBudget(double budget) {
        this.budget = budget;
    }

    public void setSatisfaction(double satisfaction) {
        this.satisfaction = satisfaction;
    }

    public void nextMonth() {
        month++;
        if (month > 12) {
            month = 1;
            year++;
        }
    }

    // Export
    @Override
    public String toString() {
        return "City | Month:" + month + " Year:" + year +
                " Budget:€" + budget + " Satisfaction:" + satisfaction + "%";
    }
}