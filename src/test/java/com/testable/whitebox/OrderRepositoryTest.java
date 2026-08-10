package com.testable.whitebox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderRepositoryTest {

    @Test
    void nPlusOneLoaderReturnsRows() {
        assertEquals(5, new OrderRepository().loadOrdersWithCustomers().size());
    }
}
