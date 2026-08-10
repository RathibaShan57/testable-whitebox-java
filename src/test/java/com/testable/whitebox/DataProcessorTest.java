package com.testable.whitebox;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataProcessorTest {

    private final DataProcessor processor = new DataProcessor();

    @Test
    void processValuesBranches() {
        assertEquals(0, processor.processValues(null));
        assertEquals(0, processor.processValues(new int[]{}));
        assertEquals(6, processor.processValues(new int[]{1, 2, 3}));
        assertEquals(150, processor.processValues(new int[]{50, 50, 50}));
    }

    @Test
    void normalizeAndPipeline() {
        assertEquals("ADA", processor.normalizeName(" ada "));
        assertNull(processor.normalizeName("   "));
        Map<String, Object> result = processor.pipeline(Arrays.asList(1, -1, null, 4));
        assertEquals(2, result.get("count"));
        assertEquals(10, result.get("total"));
    }

    @Test
    void safeLengthHappyPath() {
        assertEquals(3, processor.safeLength("abc"));
    }
}
