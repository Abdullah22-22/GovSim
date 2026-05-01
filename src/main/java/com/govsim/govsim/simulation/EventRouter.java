package com.govsim.govsim.simulation;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.ministry.*;

import java.util.HashMap;
import java.util.Map;

/** SP2 — Routes each Event to the correct Ministry */
public class EventRouter {

    private Map<String, Ministry> ministries = new HashMap<>();

    /** Register a ministry */
    public void addMinistry(String name, Ministry ministry) {
        ministries.put(name, ministry);
    }

    /** Route event to correct ministry */
    public void route(Event event) {
        Ministry ministry = ministries.get(event.getMinistry());

        if (ministry != null) {
            ministry.receiveEvent(event);
        }
    }
}