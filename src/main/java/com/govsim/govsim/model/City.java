package com.govsim.govsim.model;

/**
 * Represents the current state of the simulated city.
 * Holds budget, satisfaction, population, month, and year.
 */

public class City {

    /** The city treasury in euros. */
    private double budget;

    /** Citizen satisfaction level (0.0 - 100.0). */
    private double satisfaction;

    /** Current simulation month (1-12). */
    private int month;

    /** Current simulation year (starts at 1). */
    private int year;

    /** Fixed monthly income added to the budget each month. */
    private double monthlyIncome = 50000;

    /** Fixed monthly expenses deducted from the budget each month. */
    private double monthlyExpenses = 20000;

    /** Total population of the city. */
    private int population = 100000;

    /**
     * Creates a new City with the given starting budget.
     * @param budget the initial budget in euros
     */
    public City(double budget) {
        this.budget = budget;
        this.satisfaction = 100.0;
        this.month = 1;
        this.year = 1;

    }

    /**
     * Returns the current city budget.
     * @return budget in euros
     */
    public double getBudget() {
        return budget;
    }

    /**
     * Returns citizen satisfaction level.
     * @return satisfaction (0.0 - 100.0)
     */
    public double getSatisfaction() {
        return satisfaction;
    }

    /**
     * Returns the current simulation month.
     * @return month (1-12)
     */
    public int getMonth() {
        return month;
    }

    /**
     * Returns the current simulation year.
     * @return year starting from 1
     */
    public int getYear() {
        return year;
    }

    /**
     * Returns the city population.
     * @return population count
     */
    public int getPopulation() {
        return population;
    }

    /**
     * Applies monthly income and expenses to the budget.
     * Net change = +30,000 by default.
     */
    public void applyMonthlyFinance() {
        budget += monthlyIncome;
        budget -= monthlyExpenses;
    }

    /**
     * Sets the city budget.
     * @param budget new budget in euros
     */
    public void setBudget(double budget) {

        this.budget = budget;
    }

    /**
     * Sets the citizen satisfaction level.
     * @param satisfaction value between 0.0 and 100.0
     */
    public void setSatisfaction(double satisfaction) {

        this.satisfaction = satisfaction;
    }

    /**
     * Advances the simulation by one month.
     * Resets month to 1 and increments year after month 12.
     */
    public void nextMonth() {
        month++;
        if (month > 12) {
            month = 1;
            year++;
        }
    }

    /**
     * Sets the city population.
     * @param p new population count
     */
    public void setPopulation(int p) {
        this.population = p;
    }

    // Export
    @Override
    public String toString() {
        return "City | Month:" + month + " Year:" + year +
                " Budget:€" + budget +
                " Satisfaction:" + satisfaction + "%" +
                " Population:" + population;
    }
}