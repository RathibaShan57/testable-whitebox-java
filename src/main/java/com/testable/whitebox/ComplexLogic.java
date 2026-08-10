package com.testable.whitebox;

import java.util.HashMap;
import java.util.Map;

/**
 * ComplexLogic — high cyclomatic / cognitive complexity for CK + PMD.
 * Nested control flow triggers Checkstyle NestedIfDepth / CyclomaticComplexity.
 */
public class ComplexLogic {

    /**
     * Intentionally high complexity (CC &gt; 20) scheduler.
     * Modularization Opportunity: could be split into ≥ 5 helpers.
     */
    public Map<String, Object> scheduleTask(
            String taskType,
            String priority,
            String userRole,
            boolean resourceAvailable,
            double deadlineHours,
            int retryCount,
            boolean isCritical,
            int systemLoad) {

        Map<String, Object> result = new HashMap<>();
        result.put("status", "PENDING");
        result.put("assigned_to", null);
        result.put("eta_hours", null);

        if ("BATCH".equals(taskType)) {
            if ("HIGH".equals(priority)) {
                if (resourceAvailable) {
                    if (systemLoad < 70) {
                        result.put("status", "RUNNING");
                        result.put("assigned_to", "batch_worker_1");
                        result.put("eta_hours", 1.0);
                    } else {
                        if (retryCount < 3) {
                            result.put("status", "QUEUED");
                            result.put("eta_hours", 2.0);
                        } else {
                            result.put("status", "FAILED");
                        }
                    }
                } else {
                    result.put("status", "WAITING");
                    result.put("eta_hours", deadlineHours);
                }
            } else if ("MEDIUM".equals(priority)) {
                if (isCritical) {
                    result.put("status", "ESCALATED");
                    result.put("assigned_to", "batch_worker_2");
                } else {
                    result.put("status", "QUEUED");
                    result.put("eta_hours", deadlineHours * 2);
                }
            } else {
                result.put("status", "DEFERRED");
            }
        } else if ("REALTIME".equals(taskType)) {
            if (!resourceAvailable) {
                if (isCritical) {
                    result.put("status", "EMERGENCY_QUEUE");
                    result.put("eta_hours", 0.5);
                } else {
                    result.put("status", "DROPPED");
                }
            } else {
                if ("ADMIN".equals(userRole)) {
                    result.put("assigned_to", "rt_worker_admin");
                    result.put("status", "RUNNING");
                    result.put("eta_hours", 0.1);
                } else if ("OPERATOR".equals(userRole)) {
                    if (systemLoad < 50) {
                        result.put("assigned_to", "rt_worker_op");
                        result.put("status", "RUNNING");
                        result.put("eta_hours", 0.2);
                    } else {
                        result.put("status", "THROTTLED");
                        result.put("eta_hours", 1.0);
                    }
                } else {
                    result.put("status", "UNAUTHORIZED");
                }
            }
        } else if ("SCHEDULED".equals(taskType)) {
            if (deadlineHours <= 0) {
                result.put("status", "OVERDUE");
            } else if (deadlineHours < 1) {
                result.put("status", "URGENT");
                result.put("eta_hours", 0.5);
            } else if (deadlineHours < 24) {
                result.put("status", "NORMAL");
                result.put("eta_hours", deadlineHours * 0.8);
            } else {
                result.put("status", "FUTURE");
                result.put("eta_hours", deadlineHours);
            }
        } else {
            result.put("status", "UNKNOWN_TYPE");
        }

        String status = String.valueOf(result.get("status"));
        if ("RUNNING".equals(status) || "ESCALATED".equals(status)) {
            if (retryCount > 0) {
                result.put("status", "RETRY_RUNNING");
            }
            if (systemLoad > 90) {
                result.put("status", "OVERLOADED");
            }
        }

        return result;
    }

    /** Four independent booleans — combinatorial path coverage. */
    public boolean accessControl(boolean a, boolean b, boolean c, boolean d) {
        if (a && b) {
            return c || d;
        }
        if (c && !d) {
            return a || b;
        }
        return a && c && (b || d);
    }
}
