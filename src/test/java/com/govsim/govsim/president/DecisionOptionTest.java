package com.govsim.govsim.president;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for DecisionOption.
 */
class DecisionOptionTest {

    // ─────────────────────────────────────────────────────
    // Constructor / Getters
    // ─────────────────────────────────────────────────────

    @Test
    void testGetType() {
        DecisionOption opt = new DecisionOption(DecisionType.DANGEROUS_EVENT, "IGNORE", "Too minor", 0);
        assertEquals(DecisionType.DANGEROUS_EVENT, opt.type);
    }

    @Test
    void testGetTitle() {
        DecisionOption opt = new DecisionOption(DecisionType.DANGEROUS_EVENT, "IGNORE", "Too minor", 0);
        assertEquals("IGNORE", opt.title);
    }

    @Test
    void testGetDescription() {
        DecisionOption opt = new DecisionOption(DecisionType.MONTHLY_BUDGET, "ADD", "More funding needed", 5000);
        assertEquals("More funding needed", opt.description);
    }

    @Test
    void testGetCost() {
        DecisionOption opt = new DecisionOption(DecisionType.MONTHLY_BUDGET, "ADD", "More funding", 10000);
        assertEquals(10000, opt.cost);
    }

    @Test
    void testZeroCost() {
        DecisionOption opt = new DecisionOption(DecisionType.ANNUAL_REVIEW, "KEEP", "Good performance", 0);
        assertEquals(0, opt.cost);
    }

    @Test
    void testNegativeCost() {
        // CUT budget option — cost could be negative representing money cut
        DecisionOption opt = new DecisionOption(DecisionType.MONTHLY_BUDGET, "CUT", "Overspending", -3000);
        assertEquals(-3000, opt.cost);
    }

    // ─────────────────────────────────────────────────────
    // toString
    // ─────────────────────────────────────────────────────

    @Test
    void testToStringContainsTitle() {
        DecisionOption opt = new DecisionOption(DecisionType.DANGEROUS_EVENT, "DEPLOY", "Send troops", 2000);
        assertTrue(opt.toString().contains("DEPLOY"));
    }

    @Test
    void testToStringContainsDescription() {
        DecisionOption opt = new DecisionOption(DecisionType.DANGEROUS_EVENT, "DEPLOY", "Send troops", 2000);
        assertTrue(opt.toString().contains("Send troops"));
    }

    @Test
    void testToStringContainsCost() {
        DecisionOption opt = new DecisionOption(DecisionType.DANGEROUS_EVENT, "DEPLOY", "Send troops", 2000);
        assertTrue(opt.toString().contains("2000"));
    }

    // ─────────────────────────────────────────────────────
    // Immutability (fields are final)
    // ─────────────────────────────────────────────────────

    @Test
    void testFieldsAreFinal() throws NoSuchFieldException {
        var field = DecisionOption.class.getField("title");
        assertTrue(java.lang.reflect.Modifier.isFinal(field.getModifiers()),
                "title should be a final field");
    }

    @Test
    void testCostFieldIsFinal() throws NoSuchFieldException {
        var field = DecisionOption.class.getField("cost");
        assertTrue(java.lang.reflect.Modifier.isFinal(field.getModifiers()),
                "cost should be a final field");
    }

    // ─────────────────────────────────────────────────────
    // All DecisionTypes
    // ─────────────────────────────────────────────────────

    @Test
    void testDecisionTypeAnnualReview() {
        DecisionOption opt = new DecisionOption(DecisionType.ANNUAL_REVIEW, "FIRE", "Poor performance", 0);
        assertEquals(DecisionType.ANNUAL_REVIEW, opt.type);
    }

    @Test
    void testDecisionTypeMonthlyBudget() {
        DecisionOption opt = new DecisionOption(DecisionType.MONTHLY_BUDGET, "KEEP", "Stable", 0);
        assertEquals(DecisionType.MONTHLY_BUDGET, opt.type);
    }
}