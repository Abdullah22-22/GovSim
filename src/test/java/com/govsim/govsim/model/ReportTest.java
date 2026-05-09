package com.govsim.govsim.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the Report model class.
 */
class ReportTest {

    private Report report;

    @BeforeEach
    void setUp() {
        report = new Report("Finance", 3, 1);
    }

    // ─────────────────────────────────────────────────────
    // Constructor / Getters
    // ─────────────────────────────────────────────────────

    @Test
    void testGetMinistry() {
        assertEquals("Finance", report.getMinistry());
    }

    @Test
    void testGetMonth() {
        assertEquals(3, report.getMonth());
    }

    @Test
    void testGetYear() {
        assertEquals(1, report.getYear());
    }

    @Test
    void testInitialRating() {
        assertEquals(100.0, report.getRating(), 0.01,
                "Initial rating should be 100.0");
    }

    @Test
    void testInitialTotalEventsZero() {
        assertEquals(0, report.getTotalEvents(),
                "New report should have 0 events");
    }

    @Test
    void testInitialResolvedZero() {
        assertEquals(0, report.getResolved(),
                "New report should have 0 resolved events");
    }

    @Test
    void testInitialUnresolvedZero() {
        assertEquals(0, report.getUnresolved(),
                "New report should have 0 unresolved events");
    }

    // ─────────────────────────────────────────────────────
    // addEvent()
    // ─────────────────────────────────────────────────────

    @Test
    void testAddEventIncreasesTotal() {
        report.addEvent(new Event("Finance", "Tax fraud", Severity.NORMAL, 3));
        assertEquals(1, report.getTotalEvents());
    }

    @Test
    void testAddMultipleEvents() {
        report.addEvent(new Event("Finance", "Fraud", Severity.NORMAL, 1));
        report.addEvent(new Event("Finance", "Audit", Severity.NORMAL, 2));
        report.addEvent(new Event("Finance", "Budget cut", Severity.DANGEROUS, 3));
        assertEquals(3, report.getTotalEvents());
    }

    // ─────────────────────────────────────────────────────
    // getResolved() / getUnresolved()
    // ─────────────────────────────────────────────────────

    @Test
    void testResolvedCountWithMixedEvents() {
        Event e1 = new Event("Finance", "Issue A", Severity.NORMAL, 1);
        Event e2 = new Event("Finance", "Issue B", Severity.NORMAL, 2);
        Event e3 = new Event("Finance", "Issue C", Severity.DANGEROUS, 3);

        e1.setResolved(true);
        e2.setResolved(true);
        // e3 stays unresolved

        report.addEvent(e1);
        report.addEvent(e2);
        report.addEvent(e3);

        assertEquals(2, report.getResolved());
    }

    @Test
    void testUnresolvedCount() {
        Event e1 = new Event("Finance", "A", Severity.NORMAL, 1);
        Event e2 = new Event("Finance", "B", Severity.DANGEROUS, 2);
        e1.setResolved(true);

        report.addEvent(e1);
        report.addEvent(e2);

        assertEquals(1, report.getUnresolved());
    }

    @Test
    void testResolvedAndUnresolvedSumToTotal() {
        Event e1 = new Event("Finance", "A", Severity.NORMAL, 1);
        Event e2 = new Event("Finance", "B", Severity.NORMAL, 2);
        Event e3 = new Event("Finance", "C", Severity.DANGEROUS, 3);
        e1.setResolved(true);

        report.addEvent(e1);
        report.addEvent(e2);
        report.addEvent(e3);

        assertEquals(report.getTotalEvents(),
                report.getResolved() + report.getUnresolved(),
                "Resolved + Unresolved must equal Total");
    }

    @Test
    void testAllEventsResolved() {
        Event e1 = new Event("Finance", "A", Severity.NORMAL, 1);
        Event e2 = new Event("Finance", "B", Severity.NORMAL, 2);
        e1.setResolved(true);
        e2.setResolved(true);

        report.addEvent(e1);
        report.addEvent(e2);

        assertEquals(0, report.getUnresolved(),
                "Unresolved should be 0 when all events resolved");
    }

    // ─────────────────────────────────────────────────────
    // setRating()
    // ─────────────────────────────────────────────────────

    @Test
    void testSetRating() {
        report.setRating(72.5);
        assertEquals(72.5, report.getRating(), 0.01);
    }

    @Test
    void testSetRatingToZero() {
        report.setRating(0.0);
        assertEquals(0.0, report.getRating(), 0.01);
    }

    // ─────────────────────────────────────────────────────
    // getEvents() list
    // ─────────────────────────────────────────────────────

    @Test
    void testGetEventsNotNull() {
        assertNotNull(report.getEvents());
    }

    @Test
    void testGetEventsPreservesOrder() {
        Event e1 = new Event("Finance", "First", Severity.NORMAL, 1);
        Event e2 = new Event("Finance", "Second", Severity.NORMAL, 2);
        report.addEvent(e1);
        report.addEvent(e2);

        assertEquals("First", report.getEvents().get(0).getDescription());
        assertEquals("Second", report.getEvents().get(1).getDescription());
    }

    // ─────────────────────────────────────────────────────
    // toString
    // ─────────────────────────────────────────────────────

    @Test
    void testToStringContainsMinistry() {
        assertTrue(report.toString().contains("Finance"));
    }

    @Test
    void testToStringContainsMonth() {
        assertTrue(report.toString().contains("Month:3"));
    }

    @Test
    void testToStringContainsRating() {
        assertTrue(report.toString().contains("Rating:100.0%"));
    }
}