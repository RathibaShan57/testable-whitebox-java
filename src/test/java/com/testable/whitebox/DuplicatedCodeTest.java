package com.testable.whitebox;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DuplicatedCodeTest {

    private final DuplicatedCode dup = new DuplicatedCode();

    private List<Map<String, Object>> sampleItems() {
        Map<String, Object> item = new HashMap<>();
        item.put("price", 10.0);
        item.put("quantity", 2);
        return List.of(item);
    }

    @Test
    void orderClonesProduceTotals() {
        // 20 subtotal * 0.9 discount * 1.1 tax = 19.8
        assertEquals(19.8, ((Number) dup.processRetailOrder("R1", sampleItems(), 0.1, 0.1).get("total")).doubleValue(), 0.01);
        assertEquals(19.8, ((Number) dup.processWholesaleOrder("W1", sampleItems(), 0.1, 0.1).get("total")).doubleValue(), 0.01);
        assertEquals(19.8, ((Number) dup.processDigitalOrder("D1", sampleItems(), 0.1, 0.1).get("total")).doubleValue(), 0.01);
    }

    @Test
    void validatorsCatchMissingFields() {
        Map<String, String> bad = Map.of("name", "A");
        assertFalse(dup.validateUserInput(bad).isEmpty());
        assertFalse(dup.validateAdminInput(bad).isEmpty());
    }

    @Test
    void reportsContainHeader() {
        Map<String, Object> rec = new HashMap<>();
        rec.put("date", "2026-01-01");
        rec.put("product", "Book");
        rec.put("amount", 12.5);
        assertTrue(dup.formatSalesReport(List.of(rec)).contains("SALES"));
        assertTrue(dup.formatReturnsReport(List.of(rec)).contains("RETURNS"));
    }
}
