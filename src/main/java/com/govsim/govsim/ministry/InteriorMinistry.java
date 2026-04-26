package com.govsim.govsim.ministry;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.model.Severity;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Interior Ministry */
public class InteriorMinistry extends Ministry {

    private static final Random random = new Random();

    private int handled = 0;
    private int escalated = 0;

    private static final List<String> NORMAL_EVENTS = Arrays.asList(
            "Minor theft reported",
            "Traffic violation spike",
            "Small protest in city center",
            "Noise complaints in neighborhood",
            "Local dispute resolved");

    private static final List<String> DANGEROUS_EVENTS = Arrays.asList(
            "Massive riot in downtown",
            "Terrorist activity suspected",
            "City-wide emergency declared",
            "Police overwhelmed by violence",
            "Major prison break");

    public InteriorMinistry() {
        super("Interior");
    }

    @Override
    protected void handleNormal(Event event) {
        handled++;
        event.setResolved(true);

        System.out.println("[Interior] Handled normal event: " + event.getDescription());
    }

    @Override
    protected void handleDangerous(Event event) {
        escalated++;

        System.out.println("[Interior] DANGEROUS — sending to President: " + event.getDescription());
    }

    @Override
    public Report generateReport(int month, int year) {
        Report report = new Report(name, month, year);

        for (Event e : eventLog) {
            report.addEvent(e);
        }

        double rating = 100.0 - (escalated * 15);
        if (rating < 0)
            rating = 0;

        report.setRating(rating);

        return report;
    }

    /** Generate random event */
    public Event generateEvent(int day, Severity severity) {
        List<String> list = severity == Severity.DANGEROUS
                ? DANGEROUS_EVENTS
                : NORMAL_EVENTS;

        String desc = list.get(random.nextInt(list.size()));
        return new Event(name, desc, severity, day);
    }
}