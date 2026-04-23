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

        // EVENT LISTS
    private static final Map<String, List<String>> NORMAL_EVENTS = new HashMap<>();
    private static final Map<String, List<String>> DANGEROUS_EVENTS = new HashMap<>();

    static {
        // Defense
        NORMAL_EVENTS.put("Defense", Arrays.asList(
                "Routine military training",
                "Border patrol report",
                "Equipment maintenance"
        ));

        DANGEROUS_EVENTS.put("Defense", Arrays.asList(
                "Border invasion detected",
                "Terrorist attack threat",
                "Military base under attack"
        ));

        // Finance
        NORMAL_EVENTS.put("Finance", Arrays.asList(
                "Tax collection delayed",
                "Minor budget issue"
        ));

        DANGEROUS_EVENTS.put("Finance", Arrays.asList(
                "Budget crisis",
                "Corruption scandal"
        ));

        // Interior
        NORMAL_EVENTS.put("Interior", Arrays.asList(
                "Routine patrol",
                "Traffic violation"
        ));

        DANGEROUS_EVENTS.put("Interior", Arrays.asList(
                "Street riot",
                "Organized crime activity"
        ));

        // Population
        NORMAL_EVENTS.put("Population", Arrays.asList(
                "Housing request increase",
                "Public complaint"
        ));

        DANGEROUS_EVENTS.put("Population", Arrays.asList(
                "Mass protest",
                "Housing crisis"
        ));

        // Health
        NORMAL_EVENTS.put("Health", Arrays.asList(
                "Routine hospital check",
                "Minor flu cases"
        ));

        DANGEROUS_EVENTS.put("Health", Arrays.asList(
                "Virus outbreak",
                "Hospital overload"
        ));
    }

    // Dangerous event chance %
    private int dangerChance = 30;

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
                List<String> list = severity == Severity.DANGEROUS
                        ? DANGEROUS_EVENTS.get(ministry)
                        : NORMAL_EVENTS.get(ministry);

                String desc;

                if (list == null || list.isEmpty()) {
                    desc = "Generic event from " + ministry;
                } else {
                    desc = list.get(random.nextInt(list.size()));
                }

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