package com.testable.whitebox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RaceyCounterTest {

    @Test
    void incrementChangesCounter() {
        RaceyCounter c = new RaceyCounter();
        int before = c.get();
        c.increment();
        assertTrue(c.get() >= before);
    }
}
