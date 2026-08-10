package com.testable.whitebox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ApiClient — exercises vulnerable transitive deps for OWASP Dependency-Check:
 * commons-collections 3.2.1, jackson-databind 2.9.10, log4j-core 2.14.1.
 */
public class ApiClient {

    private static final Logger LOG = LogManager.getLogger(ApiClient.class);
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean hasItems(Collection<?> items) {
        boolean empty = CollectionUtils.isEmpty(items);
        LOG.debug("hasItems empty={}", empty);
        return !empty;
    }

    public String toJson(Map<String, Object> payload) {
        try {
            return mapper.writeValueAsString(payload == null ? Collections.emptyMap() : payload);
        } catch (Exception e) {
            LOG.error("serialize failed", e);
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fromJson(String json) {
        try {
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            LOG.warn("parse failed: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    public Map<String, Object> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("deps", hasItems(Collections.singletonList("ok")));
        return status;
    }
}
