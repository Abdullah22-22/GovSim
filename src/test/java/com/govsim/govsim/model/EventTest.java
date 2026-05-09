package com.govsim.govsim.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the Event model class.
 */
class EventTest {

    private Event normalEvent;
    private Event dangerousEvent;

    @BeforeEach
    void setUp() {
        normalEvent    = new Event("Interior", "Minor road accident", Severity.NORMAL, 5);
        dangerousEvent = new Event("Defense", "Military breach detected", Severity.DANGEROUS, 12);
    }

    // ─────────────────────────────────────────────────────
    // Constructor / Getters
    // ─────────────────────────────────────────────────────

    @Test
    void testGetMinistry() {
        assertEquals("Interior", normalEvent.getMinistry());
    }

    @Test
    void testGetDescription() {
        assertEquals("Minor road accident", normalEvent.getDescription());
    }

    @Test
    void testGetSeverityNormal() {
        assertEquals(Severity.NORMAL, normalEvent.getSeverity());
    }

    @Test
    void testGetSeverityDangerous() {
        assertEquals(Severity.DANGEROUS, dangerousEvent.getSeverity());
    }

    @Test
    void testGetDay() {
        assertEquals(5, normalEvent.getDay());
    }

    @Test
    void testGetDayDangerous() {
        assertEquals(12, dangerousEvent.getDay());
    }

    // ─────────────────────────────────────────────────────
    // Initial resolved state
    // ─────────────────────────────────────────────────────

    @Test
    void testInitialResolvedIsFalse() {
        assertFalse(normalEvent.isResolved(),
                "New events should start unresolved");
    }

    @Test
    void testDangerousEventInitialResolvedIsFalse() {
        assertFalse(dangerousEvent.isResolved(),
                "New dangerous events should start unresolved");
    }

    // ─────────────────────────────────────────────────────
    // setResolved()
    // ─────────────────────────────────────────────────────

    @Test
    void testSetResolvedTrue() {
        normalEvent.setResolved(true);
        assertTrue(normalEvent.isResolved(),
                "Event should be resolved after setResolved(true)");
    }

    @Test
    void testSetResolvedFalseAfterTrue() {
        normalEvent.setResolved(true);
        normalEvent.setResolved(false);
        assertFalse(normalEvent.isResolved(),
                "Event should be unresolved after setResolved(false)");
    }

    @Test
    void testDangerousEventCanBeResolved() {
        dangerousEvent.setResolved(true);
        assertTrue(dangerousEvent.isResolved());
    }

    // ─────────────────────────────────────────────────────
    // toString
    // ─────────────────────────────────────────────────────

    @Test
    void testToStringContainsDay() {
        assertTrue(normalEvent.toString().contains("Day 5"),
                "toString should contain the day number");
    }

    @Test
    void testToStringContainsMinistry() {
        assertTrue(normalEvent.toString().contains("Interior"),
                "toString should contain the ministry name");
    }

    @Test
    void testToStringContainsSeverity() {
        assertTrue(normalEvent.toString().contains("NORMAL"),
                "toString should contain the severity level");
    }

    @Test
    void testToStringDangerousContainsDangerous() {
        assertTrue(dangerousEvent.toString().contains("DANGEROUS"),
                "toString of dangerous event should contain DANGEROUS");
    }

    // ─────────────────────────────────────────────────────
    // Edge cases
    // ─────────────────────────────────────────────────────

    @Test
    void testEventWithDayZero() {
        Event e = new Event("Finance", "Budget error", Severity.NORMAL, 0);
        assertEquals(0, e.getDay());
    }

    @Test
    void testEventWithEmptyDescription() {
        Event e = new Event("Health", "", Severity.NORMAL, 1);
        assertEquals("", e.getDescription());
    }
}