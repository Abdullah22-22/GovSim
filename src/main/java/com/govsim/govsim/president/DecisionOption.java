package com.govsim.govsim.president;

/** One AI suggestion returned by Gemini for any decision type */
public class DecisionOption {

    public final DecisionType type;        // which case this belongs to
    public final String       title;       // main action ex: IGNORE, ADD, FIRE
    public final String       description; // short reason from Gemini
    public final int          cost;        // cost in euros (0 if not applicable)

    public DecisionOption(DecisionType type, String title, String description, int cost) {
        this.type        = type;
        this.title       = title;
        this.description = description;
        this.cost        = cost;
    }

    @Override
    public String toString() {
        return title + " - " + description + " - Cost: €" + cost;
    }
}