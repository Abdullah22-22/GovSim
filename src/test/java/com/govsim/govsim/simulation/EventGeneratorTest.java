package com.govsim.govsim.simulation;

import com.govsim.govsim.model.*;
import com.govsim.govsim.ministry.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;




import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for EventGenerator (Service Point 1 — event generation).
 */
class EventGeneratorTest {

    private EventGenerator generator;
    private List<Ministry> ministries;

    @BeforeEach
    void setUp() {
        generator = new EventGenerator();

        ministries = new ArrayList<>();
        ministries.add(new InteriorMinistry());
        ministries.add(new DefenseMinistry());
        ministries.add(new FinanceMinistry());
        ministries.add(new HealthMinistry());
        ministries.add(new PopulationMinistry());

        generator.setMinistries(ministries);
    }

    // ─────────────────────────────────────────────────────
    // Basic generation
    // ─────────────────────────────────────────────────────

    @Test
    void testGenerateDailyEventsReturnsNonNullList() {
        List<Event> events = generator.generateDailyEvents(1);
        assertNotNull(events, "Generated event list must not be null");
    }

    @Test
    void testGenerateDailyEventsDoesNotExceedMinistriesCount() {
        // At most one event per ministry per day
        List<Event> events = generator.generateDailyEvents(5);
        assertTrue(events.size() <= ministries.size(),
                "Cannot generate more events than ministries per day");
    }

    @Test
    void testGenerateDailyEventsHaveCorrectDayTag() {
        List<Event> events = generator.generateDailyEvents(7);
        for (Event e : events) {
            assertEquals(7, e.getDay(),
                    "All events should have day=7");
        }
    }

    @Test
    void testGenerateDailyEventsHaveNonNullMinistry() {
        List<Event> events = generator.generateDailyEvents(3);
        for (Event e : events) {
            assertNotNull(e.getMinistry(),
                    "Every event must have a non-null ministry");
        }
    }

    @Test
    void testGenerateDailyEventsHaveNonNullDescription() {
        List<Event> events = generator.generateDailyEvents(3);
        for (Event e : events) {
            assertNotNull(e.getDescription(),
                    "Every event must have a non-null description");
        }
    }

    @Test
    void testGenerateDailyEventsHaveValidSeverity() {
        // Run many days to ensure we get events
        for (int day = 1; day <= 30; day++) {
            List<Event> events = generator.generateDailyEvents(day);
            for (Event e : events) {
                assertTrue(
                        e.getSeverity() == Severity.NORMAL ||
                                e.getSeverity() == Severity.DANGEROUS,
                        "Severity must be either NORMAL or DANGEROUS"
                );
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // setDangerChance()
    // ─────────────────────────────────────────────────────

    @Test
    void testSetDangerChance100ProducesDangerousEvents() {
        generator.setDangerChance(100); // 100% dangerous
        boolean foundDangerous = false;
        for (int day = 1; day <= 50; day++) {
            List<Event> events = generator.generateDailyEvents(day);
            for (Event e : events) {
                if (e.getSeverity() == Severity.DANGEROUS) {
                    foundDangerous = true;
                }
            }
        }
        assertTrue(foundDangerous,
                "With 100% danger chance, at least one DANGEROUS event should appear over 50 days");
    }

    @Test
    void testSetDangerChance0ProducesOnlyNormalEvents() {
        generator.setDangerChance(0); // 0% dangerous
        for (int day = 1; day <= 30; day++) {
            List<Event> events = generator.generateDailyEvents(day);
            for (Event e : events) {
                assertEquals(Severity.NORMAL, e.getSeverity(),
                        "With 0% danger chance, all events should be NORMAL");
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // No ministries registered
    // ─────────────────────────────────────────────────────

    @Test
    void testGenerateWithNoMinistriesRegistered() {
        EventGenerator emptyGen = new EventGenerator();
        List<Event> events = emptyGen.generateDailyEvents(1);
        assertNotNull(events);
        assertTrue(events.isEmpty(),
                "No events should be generated if no ministries are registered");
    }

    // ─────────────────────────────────────────────────────
    // Determinism over multiple calls
    // ─────────────────────────────────────────────────────

    @Test
    void testGenerationIsNotAlwaysEmpty() {
        // With 5 ministries at 10-15% each, over 100 days we should get some events
        int totalEvents = 0;
        for (int day = 1; day <= 100; day++) {
            totalEvents += generator.generateDailyEvents(day).size();
        }
        assertTrue(totalEvents > 0,
                "Over 100 days, at least some events should be generated");
    }
}