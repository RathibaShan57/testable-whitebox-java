package com.testable.whitebox;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance Code (Repository) triggers:
 * - Lizard / PMD: nested loop depth &gt;= 3
 * - Semgrep / PMD AST: large allocations inside loops
 */
public class PerformanceHotspots {

    /** Nested loops deeper than 3 — Performance Hotspots / Big-O risk. */
    public int tripleNestedSum(int n) {
        int sum = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    for (int m = 0; m < 2; m++) {
                        sum += i + j + k + m;
                    }
                }
            }
        }
        return sum;
    }

    /** Object creation inside loops — Memory Allocation Pattern Analysis. */
    public List<String> allocateInLoop(int n) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            byte[] buffer = new byte[1024 * 64];
            out.add(new String(buffer) + i);
        }
        return out;
    }
}
