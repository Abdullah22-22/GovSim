package com.govsim.govsim.president;

import java.lang.reflect.Type;

/**
 * Represents one AI suggestion for any decision type.
 * Used for dangerous events, monthly reports, and annual reviews.
 */
public class DecisionOption {

    public enum type {
        DANGEROUS_EVENT, // 3 options for dangerous event
        MONTHLY_BUDGET, // 3 budget suggestions per ministry
        ANNUAL_REVIEW // keep or fire minister
    }

    public final Type type;
    public final String title; // main action ex: IGNORE, ADD, FIRE
    public final String description; // short reason
    public final int cost; // euros (0 if not applicable)

    public DecisionOption(Type type, String title, String description, int cost) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.cost = cost;
    }

    @Override
    public String toString() {
        return title + " - " + description + " - Cost: €" + cost;
    }
}
