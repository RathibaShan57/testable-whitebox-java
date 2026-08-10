package com.testable.whitebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataProcessor — def-use / data-flow patterns for SpotBugs NP_* + JaCoCo.
 * Dead assignments, null flows, and inter-method data movement.
 */
public class DataProcessor {

    private String lastError;

    /** Def without use on some paths (dead data). */
    public int processValues(int[] values) {
        int sum = 0;
        int unusedAccumulator = 0; // defined, never meaningfully used
        if (values == null || values.length == 0) {
            lastError = "empty";
            return 0;
        }
        for (int v : values) {
            sum += v;
            unusedAccumulator = unusedAccumulator + 1;
        }
        int ghost = sum * 2; // may be unused depending on branch
        if (sum > 100) {
            return sum;
        }
        return ghost > 0 ? sum : 0;
    }

    /** Possible null dereference paths for SpotBugs. */
    public String normalizeName(String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String upper = trimmed.toUpperCase();
        return upper;
    }

    public int safeLength(String maybeNull) {
        // Intentionally may NPE — SpotBugs NP_NULL_ON_SOME_PATH
        return maybeNull.length();
    }

    /** Inter-procedural data flow: transform → aggregate. */
    public Map<String, Object> pipeline(List<Integer> raw) {
        List<Integer> cleaned = transform(raw);
        return aggregate(cleaned);
    }

    private List<Integer> transform(List<Integer> raw) {
        List<Integer> out = new ArrayList<>();
        if (raw == null) {
            return out;
        }
        for (Integer v : raw) {
            if (v != null && v >= 0) {
                out.add(v * 2);
            }
        }
        return out;
    }

    private Map<String, Object> aggregate(List<Integer> values) {
        Map<String, Object> result = new HashMap<>();
        int total = 0;
        for (Integer v : values) {
            total += v;
        }
        result.put("count", values.size());
        result.put("total", total);
        result.put("avg", values.isEmpty() ? 0.0 : (double) total / values.size());
        result.put("lastError", lastError);
        return result;
    }
}
