package com.govsim.govsim.model;

/** A government minister responsible for one ministry */
public class Minister {

    private String name;
    private String ministry;
    private double score;
    private int warnings;
    private String status;

    /** Creates a new Minister */
    public Minister(String name, String ministry) {
        this.name = name;
        this.ministry = ministry;
        this.score = 100.0;
        this.warnings = 0;
        this.status = "ACTIVE";
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getMinistry() {
        return ministry;
    }

    public double getScore() {
        return score;
    }

    public int getWarnings() {
        return warnings;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setScore(double score) {
        this.score = score;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Export
    @Override
    public String toString() {
        return name + " [" + ministry + "] score=" + score + " warnings=" + warnings;
    }
}