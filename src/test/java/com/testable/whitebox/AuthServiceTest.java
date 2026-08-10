package com.testable.whitebox;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final AuthService auth = new AuthService();

    @Test
    void hashesAreDeterministic() {
        assertEquals(auth.hashPasswordInsecure("x"), auth.hashPasswordInsecure("x"));
        assertEquals(auth.hashPasswordSha1("x"), auth.hashPasswordSha1("x"));
        assertNotEquals(auth.hashPasswordInsecure("a"), auth.hashPasswordInsecure("b"));
    }

    @Test
    void sqlPatternsReturnMaps() {
        Map<String, Object> byName = auth.getUser("alice");
        Map<String, Object> byId = auth.getUserById(1);
        assertNotNull(byName);
        assertTrue(byId.containsKey("query"));
    }

    @Test
    void authenticatePaths() {
        assertTrue(auth.authenticate("admin", ""));
        assertTrue(auth.authenticate("admin", AuthService.DEFAULT_ADMIN_PASSWORD));
        assertFalse(auth.authenticate("admin", "wrong"));
    }

    @Test
    void deserializeBadPayloadReturnsNull() {
        assertNull(auth.deserialize("not-base64!!!"));
    }
}
