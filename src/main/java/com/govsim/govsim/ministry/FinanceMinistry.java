package com.govsim.govsim.ministry;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.model.Severity;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** SP3-C — Finance Ministry */

public class FinanceMinistry extends Ministry {

    private static Random random = new Random();
    // Variable income/expenses
    private double variableIncome = 0;
    private double variableExpenses = 0;

    private static final List<String> NORMAL_EVENTS = Arrays.asList(
            "Tax collection delayed",
            "Minor budget overrun",
            "Audit report issued",
            "Small corruption complaint",
            "Late salary payment");

    private static final List<String> DANGEROUS_EVENTS = Arrays.asList(
            "Budget crisis — city near bankruptcy",
            "Major corruption scandal discovered",
            "Tax system failure — no income",
            "Emergency spending required",
            "City debt reaches critical level");

    public FinanceMinistry() {
        super("Finance");
    }

    // President changes these later
    public void addVariableIncome(double amount) {
        variableIncome += amount;
    }

    public void addVariableExpense(double amount) {
        variableExpenses += amount;
    }

    @Override
    protected void handleNormal(Event event) {
        event.setResolved(true);
        System.out.println("[Finance] Normal handled: " + event);
    }

    @Override
    protected void handleDangerous(Event event) {
        System.out.println("[Finance] DANGEROUS — sending to President: " + event);
    }

    @Override
    public Report generateReport(int month, int year) {
        Report report = new Report((name), month, year);
        for (Event e : eventLog)
            report.addEvent(e);
        return report;
    }

    /** Returns a random event */
    public Event generateEvent(int day, Severity severity) {
        List<String> list = severity == Severity.DANGEROUS
                ? DANGEROUS_EVENTS
                : NORMAL_EVENTS;
        String desc = list.get(random.nextInt(list.size()));
        return new Event(name, desc, severity, day);
    }

}