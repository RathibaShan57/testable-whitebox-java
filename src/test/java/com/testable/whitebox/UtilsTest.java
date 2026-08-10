package com.testable.whitebox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    private final Utils utils = new Utils();

    @Test
    void basicHelpers() {
        assertEquals(12, utils.calc(2, 2, 3));
        assertEquals(42, utils.unusedVarsExample());
        assertEquals(8, utils.noDocstringFunction(4));
        assertEquals(5, utils.badStyleAggregated(2, 3));
        assertTrue(utils.getLongDescription(1, "n", "c", 9.99, "USD").contains("Item ID=1"));
        assertEquals("user-9", utils.getUserName("9"));
        assertEquals(2, utils.get_user_age("ab"));
    }

    @Test
    void highBranchCount() {
        assertNotNull(utils.highBranchCount(1, 1, 1, 1, 1, 1));
        assertEquals("OVERFLOW", utils.highBranchCount(10, 10, 10, 10, 10, 10));
        assertEquals("ZERO", utils.highBranchCount(0, 0, 0, 0, 0, 0));
    }
}
