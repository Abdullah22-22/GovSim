package com.govsim.govsim.president;

import com.govsim.govsim.model.City;
import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Severity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DecisionImpact {

    private static class PendingConsequence {
        int triggerDay;
        Event originalEvent;
        int choiceIndex;

        PendingConsequence(int triggerDay, Event originalEvent, int choiceIndex) {
            this.triggerDay = triggerDay;
            this.originalEvent = originalEvent;
            this.choiceIndex = choiceIndex;
        }
    }

    private final List<PendingConsequence> pending = new ArrayList<>();
    private final List<Event> pendingEvents = new ArrayList<>();
    private final Random random = new Random();

    private static final int DELAY_DAYS = 3;

    public void schedule(int currentDay, Event event, int choiceIndex) {
        int triggerDay = currentDay + DELAY_DAYS;
        pending.add(new PendingConsequence(triggerDay, event, choiceIndex));
        System.out.println("[DecisionImpact] Consequence scheduled for Day " + triggerDay);
    }

    public List<Event> processDailyConsequences(int currentDay, City city) {
        List<PendingConsequence> toRemove = new ArrayList<>();

        for (PendingConsequence c : pending) {
            if (c.triggerDay <= currentDay) {
                trigger(c, currentDay, city);
                toRemove.add(c);
            }
        }

        pending.removeAll(toRemove);

        List<Event> events = new ArrayList<>(pendingEvents);
        pendingEvents.clear();
        return events;
    }

    private void trigger(PendingConsequence c, int currentDay, City city) {
        switch (c.choiceIndex) {

            case 0 -> {
                String desc  = c.originalEvent.getDescription();
                int    count = 1;

                if (desc.contains("WORSENED x")) {
                    int start = desc.indexOf("WORSENED x") + 10;
                    int end   = desc.indexOf(":", start);
                    count = Integer.parseInt(desc.substring(start, end).trim()) + 1;
                    desc  = desc.replaceAll("WORSENED x\\d+: ", "")
                            .replace(" — crisis escalated!", "");
                } else if (desc.contains("UNRESOLVED x")) {
                    desc = desc.replaceAll("UNRESOLVED x\\d+: ", "")
                            .replace(" — still ongoing!", "");
                }

                Event worsened = new Event(
                        c.originalEvent.getMinistry(),
                        "WORSENED x" + count + ": " + desc + " — crisis escalated!",
                        Severity.DANGEROUS,
                        currentDay
                );

                System.out.println("[DecisionImpact] Ignored — situation worsened x" + count + "!");
                city.setSatisfaction(Math.max(0, city.getSatisfaction() - 5));
                city.setPopulation(Math.max(0, city.getPopulation() - 1000));
                pendingEvents.add(worsened);
            }

            case 1 -> {
                if (random.nextBoolean()) {
                    String desc  = c.originalEvent.getDescription();
                    int    count = 1;

                    if (desc.contains("UNRESOLVED x")) {
                        int start = desc.indexOf("UNRESOLVED x") + 12;
                        int end   = desc.indexOf(":", start);
                        count = Integer.parseInt(desc.substring(start, end).trim()) + 1;
                        desc  = desc.replaceAll("UNRESOLVED x\\d+: ", "")
                                .replace(" — still ongoing!", "");
                    } else if (desc.contains("WORSENED x")) {
                        desc = desc.replaceAll("WORSENED x\\d+: ", "")
                                .replace(" — crisis escalated!", "");
                    }

                    Event partial = new Event(
                            c.originalEvent.getMinistry(),
                            "UNRESOLVED x" + count + ": " + desc + " — still ongoing!",
                            Severity.DANGEROUS,
                            currentDay
                    );

                    System.out.println("[DecisionImpact] Partial — event unresolved x" + count + "!");
                    city.setPopulation(Math.max(0, city.getPopulation() - 200));
                    city.setSatisfaction(Math.max(0, city.getSatisfaction() - 2));
                    pendingEvents.add(partial);

                } else {
                    city.setPopulation(city.getPopulation() + 100);
                    city.setSatisfaction(Math.min(100, city.getSatisfaction() + 1));
                    System.out.println("[DecisionImpact] Partial — situation resolved itself.");
                }
            }

            case 2 -> {
                city.setPopulation(city.getPopulation() + 300);
                city.setSatisfaction(Math.min(100, city.getSatisfaction() + 2));
                System.out.println("[DecisionImpact] Full response — population growing!");
            }
        }
    }
}