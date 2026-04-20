package com.govsim.govsim.ministry;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.model.Severity;
import java.util.ArrayList;
import java.util.List;

/** Abstract base class for all ministries — SP3 */
public abstract class Ministry {

    protected String name;
    protected List<Event> eventLog = new ArrayList<>();

    public Ministry(String name) {
        this.name = name;
    }

    /** Receives event and decides: NORMAL or DANGEROUS */
    public void receiveEvent(Event event) {
        eventLog.add(event);
        if (event.getSeverity() == Severity.NORMAL) {
            handleNormal(event);
        } else {
            handleDangerous(event);
        }
    }

    /** Handle normal event — each ministry implements this */
    protected abstract void handleNormal(Event event);

    /** Handle dangerous event — sends to president */
    protected abstract void handleDangerous(Event event);

    /** Generate monthly report */
    public abstract Report generateReport(int month, int year);

    public String getName() {
        return name;
    }
}