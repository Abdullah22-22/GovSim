package com.govsim.govsim.ministry;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.model.Severity;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** SP3-D — Health Ministry */
public class HealthMinistry extends Ministry {

    private static final Random random = new Random();

    private static final List<String> NORMAL_EVENTS = Arrays.asList(
            "Hospital overcrowding reported",
            "Medication shortage in clinic",
            "Doctor strike warning issued",
            "Minor food poisoning outbreak",
            "Ambulance delay complaint"
    );

    private static final List<String> DANGEROUS_EVENTS = Arrays.asList(
            "Epidemic spreading across city",
            "Hospital system collapse",
            "Toxic chemical leak — mass casualties",
            "Pandemic alert declared",
            "Contaminated water supply detected"
    );

    public HealthMinistry() {
        super("Health");
    }

    @Override
    protected void handleNormal(Event event) {
        event.setResolved(true);
        System.out.println("[Health] Normal handled: " + event);
    }

    @Override
    protected void handleDangerous(Event event) {
        System.out.println("[Health] DANGEROUS — sending to President: " + event);
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