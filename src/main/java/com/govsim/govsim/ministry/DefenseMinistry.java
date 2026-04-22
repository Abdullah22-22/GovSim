package com.govsim.govsim.ministry;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.model.Severity;
import com.govsim.govsim.president.President;

/** Defense Ministry implementation */
public class DefenseMinistry extends Ministry {

    private int handled = 0;
    private int escalated = 0;

    private President president;

    public DefenseMinistry(President president) {
        super("Defense");
        this.president = president;
    }

    @Override
    protected void handleNormal(Event event) {
        handled++;
        event.setResolved(true);

        System.out.println("[Defense] Handled normal event: " + event.getDescription());
    }

    @Override
    protected void handleDangerous(Event event) {
        escalated++;

        System.out.println("[Defense] ESCALATING to President: " + event);

        // send to president
        if (president != null) {
            president.decide(event);
        } else {
            System.out.println("[Defense] No President assigned! Event unresolved.");
        }
    }

    @Override
    public Report generateReport(int month, int year) {
        Report report = new Report(name, month, year);

        for (Event e : eventLog) {
            report.addEvent(e);
        }

        double rating = 100.0 - (escalated * 15);
        if (rating < 0) rating = 0;

        report.setRating(rating);

        return report;
    }
}