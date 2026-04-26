package com.govsim.govsim.simulation;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Severity;
import java.util.*;

/** SP1 — Generates random daily events for each ministry */
public class EventGenerator {

    private static final Random random = new Random();

    // Chance % that each ministry gets an event per day
    private static final Map<String, Integer> MINISTRY_CHANCE = new LinkedHashMap<>();

    static {
        MINISTRY_CHANCE.put("Interior", 40);
        MINISTRY_CHANCE.put("Defense", 30);
        MINISTRY_CHANCE.put("Finance", 35);
        MINISTRY_CHANCE.put("Population", 40);
        MINISTRY_CHANCE.put("Health", 35);
    }

    // Dangerous event chance %
    private int dangerChance = 15;


    /** Generates events for all ministries on a given day */
    public List<Event> generateDailyEvents(int day) {
        List<Event> dailyEvents = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : MINISTRY_CHANCE.entrySet()) {
            String ministry = entry.getKey();
            int chance = entry.getValue();

            if (random.nextInt(100) < chance) {
                Severity severity = random.nextInt(100) < dangerChance
                        ? Severity.DANGEROUS
                        : Severity.NORMAL;

                dailyEvents.add(
                        new Event(ministry, "Event from " + ministry, severity, day));
            }
        }

        return dailyEvents;
    }

    /** Change dangerous event chance (default 30%) */
    public void setDangerChance(int percent) {
        this.dangerChance = percent;
    }
}