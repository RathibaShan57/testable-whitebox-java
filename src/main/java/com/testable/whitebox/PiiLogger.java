package com.testable.whitebox;

import java.util.logging.Logger;

/**
 * Compliance Code — PII in log statements (Semgrep custom rules).
 * Also embeds fixture-like PII strings for secret/PII scanners.
 */
public class PiiLogger {

    private static final Logger LOG = Logger.getLogger(PiiLogger.class.getName());

    // Fixture PII for Presidio / Semgrep (intentional — sample repo only)
    public static final String SAMPLE_SSN = "123-45-6789";
    public static final String SAMPLE_EMAIL = "jane.doe@school.edu";
    public static final String SAMPLE_PAN = "4111111111111111";

    public void logStudentRecord(String name, String ssn, String email) {
        LOG.info("Processing student name=" + name + " ssn=" + ssn + " email=" + email);
        System.out.println("PAN on file: " + SAMPLE_PAN);
    }

    public void logDefaultFixture() {
        logStudentRecord("Jane Doe", SAMPLE_SSN, SAMPLE_EMAIL);
    }
}
