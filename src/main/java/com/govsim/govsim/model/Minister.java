package com.govsim.govsim.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a government minister responsible for one ministry.
 * Tracks performance score, warnings, status, and monthly reports.
 */

public class Minister {

    /** The minister's full name. */
    private String name;
    /** The ministry this minister manages. */
    private String ministry;
    /** Performance score (0.0 - 100.0). */
    private double score;
    /** Number of warnings received. */
    private int warnings;
    /** Current status: ACTIVE or FIRED. */
    private String status;

    /** List of monthly reports used in annual review. */
    private List<Report> monthlyReports = new ArrayList<>();

    /**
     * Creates a new Minister with full score and no warnings.
     * @param name     the minister's name
     * @param ministry the ministry name
     */
    public Minister(String name, String ministry) {
        this.name = name;
        this.ministry = ministry;
        this.score = 100.0;
        this.warnings = 0;
        this.status = "ACTIVE";
    }

    // Getters
    /**
     * Returns the minister's name.
     * @return name string
     */

    public String getName() {
        return name;
    }
    /**
     * Returns the ministry this minister manages.
     * @return ministry name
     */
    public String getMinistry() {
        return ministry;
    }
    /**
     * Returns the minister's current performance score.
     * @return score (0.0 - 100.0)
     */
    public double getScore() {
        return score;
    }
    /**
     * Returns the number of warnings this minister has received.
     * @return warning count
     */
    public int getWarnings() {
        return warnings;
    }
    /**
     * Returns the minister's current status.
     * @return ACTIVE or FIRED
     */
    public String getStatus() {
        return status;
    }
    /**
     * Returns all monthly reports for this minister.
     * @return list of Report objects
     */
    public List<Report> getMonthlyReports() {
        return monthlyReports;
    }

    // Setters
    /**
     * Sets the minister's performance score.
     * @param score new score value
     */
    public void setScore(double score) {
        this.score = score;
    }
    /**
     * Sets the number of warnings.
     * @param warnings new warning count
     */
    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }
    /**
     * Sets the minister's status.
     * @param status ACTIVE or FIRED
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Adds a monthly report to this minister's record.
     * Used during annual review to evaluate performance.
     * @param r the Report to add
     */
    public void addMonthlyReport(Report r) {
        monthlyReports.add(r);
    }

    // Export
    @Override
    public String toString() {
        return name + " [" + ministry + "] score=" + score + " warnings=" + warnings;
    }
}