package com.govsim.govsim.ministry;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.model.Severity;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** SP3-E — Population Ministry */
public class PopulationMinistry extends Ministry {

    private static final Random random = new Random();

    private static final List<String> NORMAL_EVENTS = Arrays.asList(
            "Steady birth rate maintained",
            "Positive population growth",
            "Migration flow stable",
            "Demographic survey completed",
            "Birth registration updated"
    );

    private static final List<String> DANGEROUS_EVENTS = Arrays.asList(
            "Sudden population decline — crisis",
            "Mass migration — population exodus",
            "High birth rate overwhelming healthcare",
            "Demographic shift threatens economy",
            "Population growth crisis — resources depleted"
    );

    public PopulationMinistry() {
        super("Population");
    }

    @Override
    protected void handleNormal(Event event) {
        event.setResolved(true);
        System.out.println("[Population] Normal handled: " + event);
    }

    @Override
    protected void handleDangerous(Event event) {
        System.out.println("[Population] DANGEROUS — sending to President: " + event);
    }

    @Override
    public Report generateReport(int month, int year) {
        Report report = new Report(name, month, year);
        for (Event e : eventLog) report.addEvent(e);
        double rating = eventLog.isEmpty() ? 100.0
                : (report.getResolved() * 100.0 / report.getTotalEvents());
        report.setRating(rating);
        return report;
    }

    @Override
    public Event generateEvent(int day, Severity severity) {
        List<String> list = severity == Severity.DANGEROUS
                ? DANGEROUS_EVENTS : NORMAL_EVENTS;
        String desc = list.get(random.nextInt(list.size()));
        return new Event(name, desc, severity, day);
    }
}

