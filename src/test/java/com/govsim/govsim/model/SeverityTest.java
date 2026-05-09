package com.govsim.govsim.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the Severity enum.
 */
class SeverityTest {

    @Test
    void testNormalValueExists() {
        assertNotNull(Severity.NORMAL);
    }

    @Test
    void testDangerousValueExists() {
        assertNotNull(Severity.DANGEROUS);
    }

    @Test
    void testOnlyTwoValues() {
        assertEquals(2, Severity.values().length,
                "Severity enum should have exactly 2 values");
    }

    @Test
    void testNormalName() {
        assertEquals("NORMAL", Severity.NORMAL.name());
    }

    @Test
    void testDangerousName() {
        assertEquals("DANGEROUS", Severity.DANGEROUS.name());
    }

    @Test
    void testValueOfNormal() {
        assertEquals(Severity.NORMAL, Severity.valueOf("NORMAL"));
    }

    @Test
    void testValueOfDangerous() {
        assertEquals(Severity.DANGEROUS, Severity.valueOf("DANGEROUS"));
    }

    @Test
    void testNormalAndDangerousAreDistinct() {
        assertNotEquals(Severity.NORMAL, Severity.DANGEROUS);
    }

    @Test
    void testEnumOrdinalNormal() {
        assertEquals(0, Severity.NORMAL.ordinal(),
                "NORMAL should be first (ordinal 0)");
    }

    @Test
    void testEnumOrdinalDangerous() {
        assertEquals(1, Severity.DANGEROUS.ordinal(),
                "DANGEROUS should be second (ordinal 1)");
    }
}