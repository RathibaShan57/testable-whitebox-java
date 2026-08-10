package com.testable.whitebox;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Black Box / API contract smoke (Pact-style provider stub via live local server).
 */
class SampleApiContractTest {

    private SampleApiServer server;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @BeforeEach
    void start() throws Exception {
        server = new SampleApiServer(18089);
        server.start();
        Thread.sleep(200);
    }

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void healthContract() throws Exception {
        HttpResponse<String> res = client.send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:18089/health")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("ok"));
    }

    @Test
    void ordersContract() throws Exception {
        HttpResponse<String> res = client.send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:18089/api/orders")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("customerId"));
    }
}
