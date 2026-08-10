package com.testable.whitebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DuplicatedCode — intentional copy-paste clones for PMD CPD.
 * Three near-identical order processors + two validators + two formatters.
 */
public class DuplicatedCode {

    // ── Clone Group A: order processing (3 clones) ───────────────────────────

    public Map<String, Object> processRetailOrder(
            String orderId, List<Map<String, Object>> items, double discount, double taxRate) {
        double subtotal = 0.0;
        for (Map<String, Object> item : items) {
            double price = ((Number) item.getOrDefault("price", 0.0)).doubleValue();
            int qty = ((Number) item.getOrDefault("quantity", 1)).intValue();
            subtotal += price * qty;
        }
        double afterDiscount = subtotal * (1 - discount);
        double total = afterDiscount * (1 + taxRate);
        Map<String, Object> result = new HashMap<>();
        result.put("order_id", orderId);
        result.put("subtotal", Math.round(subtotal * 100.0) / 100.0);
        result.put("discount_applied", Math.round((subtotal - afterDiscount) * 100.0) / 100.0);
        result.put("tax", Math.round((total - afterDiscount) * 100.0) / 100.0);
        result.put("total", Math.round(total * 100.0) / 100.0);
        result.put("channel", "RETAIL");
        return result;
    }

    public Map<String, Object> processWholesaleOrder(
            String orderId, List<Map<String, Object>> items, double discount, double taxRate) {
        double subtotal = 0.0;
        for (Map<String, Object> item : items) {
            double price = ((Number) item.getOrDefault("price", 0.0)).doubleValue();
            int qty = ((Number) item.getOrDefault("quantity", 1)).intValue();
            subtotal += price * qty;
        }
        double afterDiscount = subtotal * (1 - discount);
        double total = afterDiscount * (1 + taxRate);
        Map<String, Object> result = new HashMap<>();
        result.put("order_id", orderId);
        result.put("subtotal", Math.round(subtotal * 100.0) / 100.0);
        result.put("discount_applied", Math.round((subtotal - afterDiscount) * 100.0) / 100.0);
        result.put("tax", Math.round((total - afterDiscount) * 100.0) / 100.0);
        result.put("total", Math.round(total * 100.0) / 100.0);
        result.put("channel", "WHOLESALE");
        return result;
    }

    public Map<String, Object> processDigitalOrder(
            String orderId, List<Map<String, Object>> items, double discount, double taxRate) {
        double subtotal = 0.0;
        for (Map<String, Object> item : items) {
            double price = ((Number) item.getOrDefault("price", 0.0)).doubleValue();
            int qty = ((Number) item.getOrDefault("quantity", 1)).intValue();
            subtotal += price * qty;
        }
        double afterDiscount = subtotal * (1 - discount);
        double total = afterDiscount * (1 + taxRate);
        Map<String, Object> result = new HashMap<>();
        result.put("order_id", orderId);
        result.put("subtotal", Math.round(subtotal * 100.0) / 100.0);
        result.put("discount_applied", Math.round((subtotal - afterDiscount) * 100.0) / 100.0);
        result.put("tax", Math.round((total - afterDiscount) * 100.0) / 100.0);
        result.put("total", Math.round(total * 100.0) / 100.0);
        result.put("channel", "DIGITAL");
        return result;
    }

    // ── Clone Group B: form validation (2 clones) ────────────────────────────

    public List<String> validateUserInput(Map<String, String> data) {
        List<String> errors = new ArrayList<>();
        if (data.get("name") == null || data.get("name").trim().length() < 2) {
            errors.add("Name is required and must be at least 2 characters");
        }
        if (data.get("email") == null || !data.get("email").contains("@")) {
            errors.add("A valid email address is required");
        }
        if (data.get("phone") == null || data.get("phone").length() < 10) {
            errors.add("Phone number must be at least 10 digits");
        }
        if (data.get("dob") == null || data.get("dob").isEmpty()) {
            errors.add("Date of birth is required");
        }
        return errors;
    }

    public List<String> validateAdminInput(Map<String, String> data) {
        List<String> errors = new ArrayList<>();
        if (data.get("name") == null || data.get("name").trim().length() < 2) {
            errors.add("Name is required and must be at least 2 characters");
        }
        if (data.get("email") == null || !data.get("email").contains("@")) {
            errors.add("A valid email address is required");
        }
        if (data.get("phone") == null || data.get("phone").length() < 10) {
            errors.add("Phone number must be at least 10 digits");
        }
        if (data.get("dob") == null || data.get("dob").isEmpty()) {
            errors.add("Date of birth is required");
        }
        return errors;
    }

    // ── Clone Group C: report formatters (2 clones) ──────────────────────────

    public String formatSalesReport(List<Map<String, Object>> records) {
        StringBuilder lines = new StringBuilder();
        lines.append("=== SALES REPORT ===\n");
        double total = 0.0;
        for (Map<String, Object> rec : records) {
            double amount = ((Number) rec.getOrDefault("amount", 0.0)).doubleValue();
            total += amount;
            lines.append("  ")
                    .append(rec.getOrDefault("date", "N/A"))
                    .append(" | ")
                    .append(rec.getOrDefault("product", "N/A"))
                    .append(" | $")
                    .append(String.format("%.2f", amount))
                    .append("\n");
        }
        lines.append("Total: $").append(String.format("%.2f", total)).append("\n");
        lines.append("====================\n");
        return lines.toString();
    }

    public String formatReturnsReport(List<Map<String, Object>> records) {
        StringBuilder lines = new StringBuilder();
        lines.append("=== RETURNS REPORT ===\n");
        double total = 0.0;
        for (Map<String, Object> rec : records) {
            double amount = ((Number) rec.getOrDefault("amount", 0.0)).doubleValue();
            total += amount;
            lines.append("  ")
                    .append(rec.getOrDefault("date", "N/A"))
                    .append(" | ")
                    .append(rec.getOrDefault("product", "N/A"))
                    .append(" | $")
                    .append(String.format("%.2f", amount))
                    .append("\n");
        }
        lines.append("Total: $").append(String.format("%.2f", total)).append("\n");
        lines.append("====================\n");
        return lines.toString();
    }
}
