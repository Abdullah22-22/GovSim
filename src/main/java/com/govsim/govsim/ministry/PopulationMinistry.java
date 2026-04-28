package com.govsim.govsim.ministry;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.model.Severity;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PopulationMinistry extends Ministry {
    /*  
    For the record i spend more time on gui than this  is kinda shit atm but it can be fixed
    */
    
    private static final List<String> NORMAL_EVENTS = Arrays.asList(
            "Steady birth rate maintained",
            "Positive population growth",
            "Migration flow stable",
            "Demographic survey completed",
            "Birth registration updated");

    private static final List<String> DANGEROUS_EVENTS = Arrays.asList(
            "Sudden population decline — crisis",
            "Mass migration — population exodus",
            "High birth rate overwhelming healthcare",
            "Demographic shift threatens economy",
            "Population growth crisis — resources depleted");

    private Random random = new Random();

    public PopulationMinistry() {
        super("Population");
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
    
        for (Event event : eventLog) {
            report.addEvent(event);
        }
        return report;
    }

    public void generateEvent(int day) {
        Severity severity = Severity.values()[random.nextInt(Severity.values().length)];
        List<String> eventList = severity == Severity.NORMAL ? NORMAL_EVENTS : DANGEROUS_EVENTS;
        String eventType = eventList.get(random.nextInt(eventList.size()));
        String description = "Event: " + eventType + " with severity " + severity;
        Event event = new Event(name, description, severity, day);
        receiveEvent(event);
    }
}