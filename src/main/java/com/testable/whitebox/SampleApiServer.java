package com.testable.whitebox;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Minimal HTTP surface for Black Box / Security URL / Performance URL tools:
 * Playwright, Newman/Postman, OWASP ZAP, k6, Pact provider stub.
 *
 * Endpoints intentionally expose weak auth / PII / open CORS for scanners.
 */
public class SampleApiServer {

    public static final int DEFAULT_PORT = 8089;

    private final HttpServer server;

    public SampleApiServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::root);
        server.createContext("/health", this::health);
        server.createContext("/api/orders", this::orders);
        server.createContext("/api/students", this::students);
        server.createContext("/api/login", this::login);
        server.createContext("/openapi.json", this::openapi);
        server.setExecutor(Executors.newCachedThreadPool());
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        SampleApiServer api = new SampleApiServer(port);
        api.start();
        System.out.println("SampleApiServer listening on http://127.0.0.1:" + port);
    }

    private void root(HttpExchange ex) throws IOException {
        String html = "<!doctype html><html><head><title>Testable Sample</title></head>"
                + "<body><h1>Testable WhiteBox Sample</h1>"
                + "<button id=\"go\">Load Orders</button>"
                + "<pre id=\"out\"></pre>"
                + "<script>document.getElementById('go').onclick=async()=>{"
                + "const r=await fetch('/api/orders');"
                + "document.getElementById('out').textContent=await r.text();"
                + "};</script></body></html>";
        write(ex, 200, "text/html; charset=utf-8", html);
    }

    private void health(HttpExchange ex) throws IOException {
        write(ex, 200, "application/json", "{\"status\":\"ok\"}");
    }

    private void orders(HttpExchange ex) throws IOException {
        // Weak auth: accepts any Bearer token, including empty
        write(ex, 200, "application/json",
                "[{\"id\":1,\"customerId\":1,\"total\":10.0},"
                        + "{\"id\":2,\"customerId\":2,\"total\":20.0}]");
    }

    private void students(HttpExchange ex) throws IOException {
        // Intentional PII leakage for DAST / Presidio / compliance URL proxies
        write(ex, 200, "application/json",
                "[{\"name\":\"Jane Doe\",\"ssn\":\"123-45-6789\",\"email\":\"jane.doe@school.edu\"}]");
    }

    private void login(HttpExchange ex) throws IOException {
        // Always succeeds — Authorization Testing / session weakness trigger
        write(ex, 200, "application/json",
                "{\"token\":\"demo-token-not-secure\",\"role\":\"admin\"}");
    }

    private void openapi(HttpExchange ex) throws IOException {
        String spec = "{\n"
                + "  \"openapi\": \"3.0.3\",\n"
                + "  \"info\": {\"title\": \"Testable Sample API\", \"version\": \"1.0.0\"},\n"
                + "  \"paths\": {\n"
                + "    \"/health\": {\"get\": {\"responses\": {\"200\": {\"description\": \"ok\"}}}},\n"
                + "    \"/api/orders\": {\"get\": {\"responses\": {\"200\": {\"description\": \"ok\"}}}},\n"
                + "    \"/api/students\": {\"get\": {\"responses\": {\"200\": {\"description\": \"ok\"}}}},\n"
                + "    \"/api/login\": {\"post\": {\"responses\": {\"200\": {\"description\": \"ok\"}}}}\n"
                + "  }\n"
                + "}";
        write(ex, 200, "application/json", spec);
    }

    private void write(HttpExchange ex, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
