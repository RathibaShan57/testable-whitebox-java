package com.testable.whitebox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    private final Calculator calc = new Calculator();

    @Test
    void arithmetic() {
        assertEquals(5, calc.add(2, 3));
        assertEquals(-1, calc.subtract(2, 3));
        assertEquals(6, calc.multiply(2, 3));
        assertEquals(2.5, calc.divide(5, 2));
    }

    @Test
    void divideByZero() {
        assertThrows(IllegalArgumentException.class, () -> calc.divide(1, 0));
    }

    @Test
    void classifyScoreBranches() {
        assertEquals("INVALID", calc.classifyScore(-1, false, false));
        assertEquals("EXCELLENT+", calc.classifyScore(95, true, false));
        assertEquals("EXCELLENT+", calc.classifyScore(95, false, true));
        assertEquals("GOOD", calc.classifyScore(80, false, false));
        assertEquals("AVERAGE", calc.classifyScore(55, false, false));
        assertEquals("POOR", calc.classifyScore(10, false, false));
    }

    @Test
    void gradeStudentBranches() {
        assertEquals("DETAINED", calc.gradeStudent(90, 70, true));
        assertEquals("E", calc.gradeStudent(45, 80, false));
        assertEquals("F", calc.gradeStudent(30, 80, false));
        assertEquals("A+", calc.gradeStudent(95, 80, true));
        assertEquals("A", calc.gradeStudent(85, 80, true));
        assertEquals("B", calc.gradeStudent(75, 80, true));
        assertEquals("C", calc.gradeStudent(65, 80, true));
        assertEquals("D", calc.gradeStudent(55, 80, true));
        assertEquals("F", calc.gradeStudent(40, 80, true));
    }

    @Test
    void insurancePremiumPaths() {
        double young = calc.computeInsurancePremium(22, "M", true, 1, 2, true, "URBAN", 60000);
        double senior = calc.computeInsurancePremium(70, "F", false, 10, 0, false, "COASTAL", 20000);
        assertTrue(young > 0);
        assertTrue(senior > 0);
        assertNotEquals(young, senior);
    }
}
