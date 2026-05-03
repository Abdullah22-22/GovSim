package com.govsim.govsim.ministry;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.president.President;

/**
 * Finance Ministry implementation (Budget, Tax)
 */
public class FinanceMinistry extends Ministry {

    private int handled = 0;
    private int escalated = 0;
    private President president;

    public FinanceMinistry(President president) {
        super("Finance");
        this.president = president;
    }

    @Override
    protected void handleNormal(Event event) {
        handled++;
        event.setResolved(true);
        System.out.println("[Finance] Handled normal event: " + event.getDescription());
    }

    @Override
    protected void handleDangerous(Event event) {
        escalated++;
        System.out.println("[Finance] ESCALATING to President: " + event);
        if (president != null) {
            president.decide(event);
        }
    }

    @Override
    public Report generateReport(int month, int year) {
        Report report = new Report(name, month, year);
        for (Event e : eventLog) {
            report.addEvent(e);
        }
        double rating = 100.0 - (escalated * 10);
        if (rating < 0) rating = 0;
        report.setRating(rating);
        return report;
    }
}
