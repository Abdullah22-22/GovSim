package com.govsim.govsim.model;

/**
 * Severity level of a city event.
 * Determines how the event is handled in the simulation.
 */
public enum Severity {

    /** Normal event — handled automatically by the ministry. */
    NORMAL,

    /** Dangerous event — simulation pauses and president decides. */
    DANGEROUS
}