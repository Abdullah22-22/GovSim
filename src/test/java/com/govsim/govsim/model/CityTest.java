package com.govsim.govsim.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the City model class.
 */
class CityTest {

    private City city;

    @BeforeEach
    void setUp() {
        city = new City(1_000_000);
    }

    // ─────────────────────────────────────────────────────
    // Constructor tests
    // ─────────────────────────────────────────────────────

    @Test
    void testInitialBudget() {
        assertEquals(1_000_000, city.getBudget(), 0.01,
                "Initial budget should be 1,000,000");
    }

    @Test
    void testInitialSatisfaction() {
        assertEquals(100.0, city.getSatisfaction(), 0.01,
                "Initial satisfaction should be 100.0");
    }

    @Test
    void testInitialMonth() {
        assertEquals(1, city.getMonth(),
                "Initial month should be 1");
    }

    @Test
    void testInitialYear() {
        assertEquals(1, city.getYear(),
                "Initial year should be 1");
    }

    @Test
    void testInitialPopulation() {
        assertEquals(100_000, city.getPopulation(),
                "Initial population should be 100,000");
    }

    // ─────────────────────────────────────────────────────
    // nextMonth() tests
    // ─────────────────────────────────────────────────────

    @Test
    void testNextMonthIncrementsMonth() {
        city.nextMonth();
        assertEquals(2, city.getMonth(),
                "Month should increment from 1 to 2");
    }

    @Test
    void testNextMonthWrapsAfter12() {
        for (int i = 0; i < 12; i++) city.nextMonth();
        assertEquals(1, city.getMonth(),
                "Month should wrap back to 1 after 12 months");
    }

    @Test
    void testNextMonthIncrementsYearAfter12() {
        for (int i = 0; i < 12; i++) city.nextMonth();
        assertEquals(2, city.getYear(),
                "Year should increment to 2 after 12 months");
    }

    @Test
    void testNextMonthDoesNotIncrementYearBefore12() {
        for (int i = 0; i < 11; i++) city.nextMonth();
        assertEquals(1, city.getYear(),
                "Year should still be 1 after only 11 months");
    }

    // ─────────────────────────────────────────────────────
    // applyMonthlyFinance() tests
    // ─────────────────────────────────────────────────────

    @Test
    void testApplyMonthlyFinanceIncreasesNet() {
        double before = city.getBudget();
        city.applyMonthlyFinance();
        // income=50000, expenses=20000 → net +30000
        assertEquals(before + 30_000, city.getBudget(), 0.01,
                "Budget should increase by 30,000 after monthly finance");
    }

    @Test
    void testApplyMonthlyFinanceMultipleTimes() {
        city.applyMonthlyFinance();
        city.applyMonthlyFinance();
        assertEquals(1_060_000, city.getBudget(), 0.01,
                "Budget should be 1,060,000 after two finance cycles");
    }

    // ─────────────────────────────────────────────────────
    // Setters tests
    // ─────────────────────────────────────────────────────

    @Test
    void testSetBudget() {
        city.setBudget(500_000);
        assertEquals(500_000, city.getBudget(), 0.01);
    }

    @Test
    void testSetSatisfaction() {
        city.setSatisfaction(75.5);
        assertEquals(75.5, city.getSatisfaction(), 0.01);
    }

    @Test
    void testSetPopulation() {
        city.setPopulation(200_000);
        assertEquals(200_000, city.getPopulation());
    }

    @Test
    void testSetBudgetToZero() {
        city.setBudget(0);
        assertEquals(0, city.getBudget(), 0.01);
    }

    @Test
    void testSetNegativeBudget() {
        city.setBudget(-5000);
        assertEquals(-5000, city.getBudget(), 0.01,
                "Budget can be negative (debt situation)");
    }

    // ─────────────────────────────────────────────────────
    // toString test
    // ─────────────────────────────────────────────────────

    @Test
    void testToStringContainsMonth() {
        assertTrue(city.toString().contains("Month:1"),
                "toString should contain the current month");
    }

    @Test
    void testToStringContainsYear() {
        assertTrue(city.toString().contains("Year:1"),
                "toString should contain the current year");
    }
}