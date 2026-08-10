package com.testable.whitebox;

import org.junit.jupiter.api.Test;

class PiiLoggerTest {

    @Test
    void logsFixtureWithoutThrowing() {
        new PiiLogger().logDefaultFixture();
    }
}
