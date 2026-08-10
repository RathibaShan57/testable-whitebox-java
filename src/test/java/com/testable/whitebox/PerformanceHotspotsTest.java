package com.testable.whitebox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceHotspotsTest {

    @Test
    void nestedLoopsProducePositiveSum() {
        assertTrue(new PerformanceHotspots().tripleNestedSum(2) > 0);
    }

    @Test
    void allocateInLoopReturnsItems() {
        assertFalse(new PerformanceHotspots().allocateInLoop(2).isEmpty());
    }
}
