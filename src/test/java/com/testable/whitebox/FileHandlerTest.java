package com.testable.whitebox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileHandlerTest {

    private final FileHandler handler = new FileHandler();

    @TempDir
    Path temp;

    @Test
    void writeAndReadRoundTrip() throws Exception {
        Path file = temp.resolve("sample.txt");
        assertTrue(handler.writeText(file, "hello"));
        assertEquals("hello", handler.readText(file));
        assertTrue(handler.readTextSafe(file).isPresent());
    }

    @Test
    void exceptionAndNullPaths() {
        assertTrue(handler.readTextSafe(null).isEmpty());
        assertTrue(handler.readTextSafe(temp.resolve("missing.txt")).isEmpty());
        assertFalse(handler.writeText(null, "x"));
        assertFalse(handler.writeText(temp.resolve("a.txt"), null));
    }

    @Test
    void classifySize() {
        assertEquals("INVALID", handler.classifySize(-1));
        assertEquals("EMPTY", handler.classifySize(0));
        assertEquals("TINY", handler.classifySize(10));
        assertEquals("SMALL", handler.classifySize(2048));
        assertEquals("MEDIUM", handler.classifySize(2_000_000));
    }
}
