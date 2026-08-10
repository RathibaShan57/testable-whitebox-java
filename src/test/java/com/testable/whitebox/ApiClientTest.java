package com.testable.whitebox;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiClientTest {

    private final ApiClient client = new ApiClient();

    @Test
    void hasItemsAndHealth() {
        assertFalse(client.hasItems(Collections.emptyList()));
        assertTrue(client.hasItems(Collections.singletonList(1)));
        assertEquals("UP", client.healthCheck().get("status"));
    }

    @Test
    void jsonRoundTrip() {
        String json = client.toJson(Map.of("a", 1));
        assertTrue(json.contains("\"a\""));
        Map<String, Object> parsed = client.fromJson(json);
        assertEquals(1, ((Number) parsed.get("a")).intValue());
        assertTrue(client.fromJson("not-json").isEmpty());
    }
}
