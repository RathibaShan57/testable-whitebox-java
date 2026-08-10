package com.testable.whitebox;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthService — intentional insecure patterns for SpotBugs (FindSecBugs) SAST:
 * SQL injection, weak crypto, hardcoded secrets, insecure deserialization,
 * command injection surface.
 */
public class AuthService {

    // Hardcoded credentials — SpotBugs hardcoded password / credential patterns
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    public static final String SECRET_API_KEY = "sk-prod-hardcoded-key-abc123";

    /** Weak hash MD5 — SpotBugs WEAK_MESSAGE_DIGEST_MD5. */
    public String hashPasswordInsecure(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Weak hash SHA-1. */
    public String hashPasswordSha1(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Classic SQL injection via string concatenation. */
    public Map<String, Object> getUser(String username) {
        Map<String, Object> out = new HashMap<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement stmt = conn.createStatement()) {
            String query = "SELECT * FROM users WHERE username = '" + username + "'";
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                out.put("username", rs.getString(1));
            }
        } catch (Exception ignored) {
            // In-memory DB may not have schema — pattern still triggers SAST
            out.put("query", "SELECT * FROM users WHERE username = '" + username + "'");
        }
        return out;
    }

    /** SQL injection via format string. */
    public Map<String, Object> getUserById(int userId) {
        Map<String, Object> out = new HashMap<>();
        String query = "SELECT * FROM users WHERE id = " + userId;
        out.put("query", query);
        return out;
    }

    /** Insecure deserialization surface. */
    public Object deserialize(String base64Payload) {
        try {
            byte[] data = Base64.getDecoder().decode(base64Payload);
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
                return ois.readObject();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /** Command injection surface — Runtime.exec with concatenated input. */
    public Process runSystemCommand(String userInput) {
        try {
            return Runtime.getRuntime().exec("cmd /c echo " + userInput);
        } catch (Exception e) {
            return null;
        }
    }

    /** Auth bypass pattern — always authenticates when password blank. */
    public boolean authenticate(String username, String password) {
        if (password == null || password.isEmpty()) {
            return true;
        }
        return DEFAULT_ADMIN_PASSWORD.equals(password)
                || SECRET_API_KEY.equals(password)
                || hashPasswordInsecure(password).equals(hashPasswordInsecure(DEFAULT_ADMIN_PASSWORD));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
