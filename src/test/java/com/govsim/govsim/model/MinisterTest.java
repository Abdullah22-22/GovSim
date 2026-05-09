package com.govsim.govsim.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the Minister model class.
 */
class MinisterTest {

    private Minister minister;

    @BeforeEach
    void setUp() {
        minister = new Minister("Ahmed", "Interior");
    }

    // ─────────────────────────────────────────────────────
    // Constructor / Initial state
    // ─────────────────────────────────────────────────────

    @Test
    void testGetName() {
        assertEquals("Ahmed", minister.getName());
    }

    @Test
    void testGetMinistry() {
        assertEquals("Interior", minister.getMinistry());
    }

    @Test
    void testInitialScore() {
        assertEquals(100.0, minister.getScore(), 0.01,
                "Initial score should be 100.0");
    }

    @Test
    void testInitialWarnings() {
        assertEquals(0, minister.getWarnings(),
                "Initial warnings should be 0");
    }

    @Test
    void testInitialStatus() {
        assertEquals("ACTIVE", minister.getStatus(),
                "Initial status should be ACTIVE");
    }

    @Test
    void testInitialMonthlyReportsEmpty() {
        assertTrue(minister.getMonthlyReports().isEmpty(),
                "Monthly reports list should be empty initially");
    }

    // ─────────────────────────────────────────────────────
    // Setters
    // ─────────────────────────────────────────────────────

    @Test
    void testSetScore() {
        minister.setScore(75.0);
        assertEquals(75.0, minister.getScore(), 0.01);
    }

    @Test
    void testSetWarnings() {
        minister.setWarnings(3);
        assertEquals(3, minister.getWarnings());
    }

    @Test
    void testSetStatusFired() {
        minister.setStatus("FIRED");
        assertEquals("FIRED", minister.getStatus());
    }

    @Test
    void testSetScoreToZero() {
        minister.setScore(0.0);
        assertEquals(0.0, minister.getScore(), 0.01);
    }

    // ─────────────────────────────────────────────────────
    // addMonthlyReport()
    // ─────────────────────────────────────────────────────

    @Test
    void testAddOneMonthlyReport() {
        Report r = new Report("Interior", 1, 1);
        minister.addMonthlyReport(r);
        assertEquals(1, minister.getMonthlyReports().size(),
                "Should have 1 report after adding one");
    }

    @Test
    void testAddMultipleMonthlyReports() {
        minister.addMonthlyReport(new Report("Interior", 1, 1));
        minister.addMonthlyReport(new Report("Interior", 2, 1));
        minister.addMonthlyReport(new Report("Interior", 3, 1));
        assertEquals(3, minister.getMonthlyReports().size(),
                "Should have 3 reports after adding three");
    }

    @Test
    void testMonthlyReportContentIsPreserved() {
        Report r = new Report("Interior", 6, 1);
        r.setRating(85.0);
        minister.addMonthlyReport(r);
        assertEquals(85.0, minister.getMonthlyReports().get(0).getRating(), 0.01,
                "Report rating should be preserved in the list");
    }

    @Test
    void testMonthlyReportsListIsNotNull() {
        assertNotNull(minister.getMonthlyReports());
    }

    // ─────────────────────────────────────────────────────
    // toString
    // ─────────────────────────────────────────────────────

    @Test
    void testToStringContainsName() {
        assertTrue(minister.toString().contains("Ahmed"));
    }

    @Test
    void testToStringContainsMinistry() {
        assertTrue(minister.toString().contains("Interior"));
    }

    @Test
    void testToStringContainsWarnings() {
        minister.setWarnings(2);
        assertTrue(minister.toString().contains("warnings=2"));
    }

    // ─────────────────────────────────────────────────────
    // Edge cases
    // ─────────────────────────────────────────────────────

    @Test
    void testMinisterWithDifferentMinistry() {
        Minister m = new Minister("Hassan", "Defense");
        assertEquals("Defense", m.getMinistry());
    }

    @Test
    void testScoreCanExceed100() {
        minister.setScore(110.0);
        assertEquals(110.0, minister.getScore(), 0.01,
                "Score can be set above 100 (no hard cap enforced in model)");
    }
}