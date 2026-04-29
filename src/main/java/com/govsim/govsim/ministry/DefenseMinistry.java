package com.govsim.govsim.ministry;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.model.Severity;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** SP3-B — Defense Ministry */
public class DefenseMinistry extends Ministry {

    private static final Random random = new Random();

    private static final List<String> NORMAL_EVENTS = Arrays.asList(
        "Border alert reported",
        "Military drill incident",
        "Spy detected near border",
        "Unauthorized drone spotted",
        "Soldier training accident"
    );

    private static final List<String> DANGEROUS_EVENTS = Arrays.asList(
        "Military attack on the city",
        "Base breach — soldiers missing",
        "Enemy invasion detected",
        "Nuclear threat reported",
        "Terrorist attack on military base"
    );

    public DefenseMinistry() {
        super("Defense");
    }

    @Override
    protected void handleNormal(Event event) {
        event.setResolved(true);
        System.out.println("[Defense] Normal handled: " + event);
    }

    @Override
    protected void handleDangerous(Event event) {
        System.out.println("[Defense] DANGEROUS — sending to President: " + event);
    }

    @Override
    public Report generateReport(int month, int year) {
        Report report = new Report(name, month, year);
        for (Event e : eventLog) report.addEvent(e);
        return report;
    }

    public Event generateEvent(int day, Severity severity) {
        List<String> list = severity == Severity.DANGEROUS
            ? DANGEROUS_EVENTS : NORMAL_EVENTS;
        String desc = list.get(random.nextInt(list.size()));
        return new Event(name, desc, severity, day);
    }
}