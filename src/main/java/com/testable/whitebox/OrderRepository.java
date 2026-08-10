package com.testable.whitebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * N+1 query anti-pattern stand-in for Semgrep Hibernate Rules
 * (Performance Code sheet — Database Query Analysis).
 *
 * Uses findById-style calls inside a loop over a collection (classic N+1).
 */
public class OrderRepository {

    private final Map<Long, Map<String, Object>> customers = new HashMap<>();
    private final Map<Long, Map<String, Object>> orders = new HashMap<>();

    public OrderRepository() {
        for (long i = 1; i <= 5; i++) {
            Map<String, Object> c = new HashMap<>();
            c.put("id", i);
            c.put("email", "student" + i + "@school.edu");
            customers.put(i, c);

            Map<String, Object> o = new HashMap<>();
            o.put("id", i);
            o.put("customerId", i);
            o.put("total", 10.0 * i);
            orders.put(i, o);
        }
    }

    public Map<String, Object> findCustomerById(Long id) {
        return customers.get(id);
    }

    public List<Map<String, Object>> findAllOrders() {
        return new ArrayList<>(orders.values());
    }

    /**
     * N+1: one query for all orders, then one findById per order.
     * Semgrep rule looks for find/get calls inside for-each over collections.
     */
    public List<Map<String, Object>> loadOrdersWithCustomers() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> order : findAllOrders()) {
            Long customerId = (Long) order.get("customerId");
            Map<String, Object> customer = findCustomerById(customerId);
            Map<String, Object> row = new HashMap<>(order);
            row.put("customer", customer);
            result.add(row);
        }
        return result;
    }
}
