package com.govsim.govsim.president;

/** Type of AI decision — used to identify which case the suggestion belongs to */
public enum DecisionType {
    DANGEROUS_EVENT,  // 3 options for dangerous event
    MONTHLY_BUDGET,   // ADD / CUT / KEEP per ministry budget
    ANNUAL_REVIEW     // KEEP or FIRE minister
}