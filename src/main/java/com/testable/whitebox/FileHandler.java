package com.testable.whitebox;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * FileHandler — branch / exception path coverage for JaCoCo.
 */
public class FileHandler {

    public String readText(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path required");
        }
        if (!Files.exists(path)) {
            throw new IOException("missing: " + path);
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public Optional<String> readTextSafe(Path path) {
        try {
            return Optional.of(readText(path));
        } catch (IllegalArgumentException | IOException ex) {
            return Optional.empty();
        }
    }

    public boolean writeText(Path path, String content) {
        if (path == null || content == null) {
            return false;
        }
        try {
            Files.createDirectories(path.getParent() == null
                    ? Path.of(".")
                    : path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /** Dead / unreachable-ish branch for coverage gap metrics. */
    public String classifySize(long bytes) {
        if (bytes < 0) {
            return "INVALID";
        }
        if (bytes == 0) {
            return "EMPTY";
        }
        if (bytes < 1024) {
            return "TINY";
        }
        if (bytes < 1024 * 1024) {
            return "SMALL";
        }
        if (bytes < 1024L * 1024 * 1024) {
            return "MEDIUM";
        }
        // Rarely hit in unit tests → coverage gap
        return "LARGE";
    }
}
