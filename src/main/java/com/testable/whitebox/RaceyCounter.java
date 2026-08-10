package com.testable.whitebox;

/**
 * SpotBugs Concurrency Rules — unsynchronized shared mutable state
 * (Performance Code sheet — Race Condition Risk Count).
 */
public class RaceyCounter {

    public static int GLOBAL_COUNT = 0;

    public void increment() {
        GLOBAL_COUNT = GLOBAL_COUNT + 1;
    }

    public int get() {
        return GLOBAL_COUNT;
    }
}
