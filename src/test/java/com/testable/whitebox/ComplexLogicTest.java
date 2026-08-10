package com.testable.whitebox;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComplexLogicTest {

    private final ComplexLogic logic = new ComplexLogic();

    @Test
    void batchHighRunning() {
        Map<String, Object> r = logic.scheduleTask("BATCH", "HIGH", "USER", true, 8, 0, false, 40);
        assertEquals("RUNNING", r.get("status"));
    }

    @Test
    void batchHighQueuedAndFailed() {
        assertEquals("QUEUED", logic.scheduleTask("BATCH", "HIGH", "USER", true, 8, 1, false, 80).get("status"));
        assertEquals("FAILED", logic.scheduleTask("BATCH", "HIGH", "USER", true, 8, 3, false, 80).get("status"));
    }

    @Test
    void realtimeAndScheduled() {
        assertEquals("EMERGENCY_QUEUE",
                logic.scheduleTask("REALTIME", "HIGH", "USER", false, 1, 0, true, 10).get("status"));
        assertEquals("UNAUTHORIZED",
                logic.scheduleTask("REALTIME", "HIGH", "GUEST", true, 1, 0, false, 10).get("status"));
        assertEquals("URGENT",
                logic.scheduleTask("SCHEDULED", "LOW", "USER", true, 0.5, 0, false, 10).get("status"));
        assertEquals("UNKNOWN_TYPE",
                logic.scheduleTask("OTHER", "LOW", "USER", true, 1, 0, false, 10).get("status"));
    }

    @Test
    void accessControlCombinations() {
        assertTrue(logic.accessControl(true, true, false, true));
        assertFalse(logic.accessControl(false, false, false, false));
        assertTrue(logic.accessControl(false, true, true, false));
    }
}
