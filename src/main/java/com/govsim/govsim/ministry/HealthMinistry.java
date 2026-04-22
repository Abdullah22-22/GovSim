package com.govsim.govsim.ministry;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.president.President;

public class HealthMinistry extends Ministry {

    private int handled = 0;
    private int escalated = 0;
    private President president;

    public HealthMinistry(President president) {
        super("Health");
        this.president = president;
    }

    @Override
    protected void handleNormal(Event event) {
    }

    @Override
    protected void handleDangerous(Event event) {
    }

    @Override
    public Report generateReport(int month, int year) {
        Report report = new Report(name, month, year);

        return report;
    }
}