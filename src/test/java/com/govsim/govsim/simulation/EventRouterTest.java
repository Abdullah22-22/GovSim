package com.govsim.govsim.simulation;

import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Severity;
import com.govsim.govsim.ministry.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for EventRouter (Service Point 2 — routing events to ministries).
 */
class EventRouterTest {

    private EventRouter   router;
    private InteriorMinistry interiorMinistry;
    private DefenseMinistry  defenseMinistry;

    @BeforeEach
    void setUp() {
        router           = new EventRouter();
        interiorMinistry = new InteriorMinistry();
        defenseMinistry  = new DefenseMinistry();

        router.addMinistry("Interior", interiorMinistry);
        router.addMinistry("Defense",  defenseMinistry);
    }

    // ─────────────────────────────────────────────────────
    // Routing to correct ministry
    // ─────────────────────────────────────────────────────

    @Test
    void testRouteNormalEventToInterior() {
        Event e = new Event("Interior", "Small fire", Severity.NORMAL, 1);
        router.route(e);
        assertEquals(1, interiorMinistry.getEventLog().size(),
                "Interior ministry should have received 1 event");
    }

    @Test
    void testRouteEventToDefense() {
        Event e = new Event("Defense", "Border alert", Severity.DANGEROUS, 2);
        router.route(e);
        assertEquals(1, defenseMinistry.getEventLog().size(),
                "Defense ministry should have received 1 event");
    }

    @Test
    void testRouteDoesNotCrossMinistries() {
        Event e = new Event("Interior", "Traffic incident", Severity.NORMAL, 1);
        router.route(e);
        assertEquals(0, defenseMinistry.getEventLog().size(),
                "Defense ministry should NOT receive Interior events");
    }

    @Test
    void testRouteMultipleEventsToSameMinistry() {
        router.route(new Event("Interior", "Event A", Severity.NORMAL, 1));
        router.route(new Event("Interior", "Event B", Severity.NORMAL, 2));
        assertEquals(2, interiorMinistry.getEventLog().size(),
                "Interior should have 2 events after 2 routes");
    }

    @Test
    void testRouteEventsToMultipleMinistries() {
        router.route(new Event("Interior", "Fire",    Severity.NORMAL,    1));
        router.route(new Event("Defense",  "Missile", Severity.DANGEROUS, 1));

        assertEquals(1, interiorMinistry.getEventLog().size());
        assertEquals(1, defenseMinistry.getEventLog().size());
    }

    // ─────────────────────────────────────────────────────
    // Unknown ministry (no registration)
    // ─────────────────────────────────────────────────────

    @Test
    void testRouteToUnknownMinistryDoesNotThrow() {
        Event e = new Event("Finance", "Budget issue", Severity.NORMAL, 5);
        // Finance is not registered — should silently skip
        assertDoesNotThrow(() -> router.route(e),
                "Routing to an unregistered ministry should not throw");
    }

    @Test
    void testRouteToUnknownMinistryDoesNotAffectOthers() {
        router.route(new Event("Finance", "Unknown", Severity.NORMAL, 1));
        assertEquals(0, interiorMinistry.getEventLog().size());
        assertEquals(0, defenseMinistry.getEventLog().size());
    }

    // ─────────────────────────────────────────────────────
    // addMinistry() overwrites
    // ─────────────────────────────────────────────────────

    @Test
    void testAddMinistrySameNameOverwrites() {
        InteriorMinistry newInterior = new InteriorMinistry();
        router.addMinistry("Interior", newInterior);

        Event e = new Event("Interior", "Test", Severity.NORMAL, 1);
        router.route(e);

        // New ministry should have the event; old one should not
        assertEquals(1, newInterior.getEventLog().size());
        assertEquals(0, interiorMinistry.getEventLog().size());
    }
}