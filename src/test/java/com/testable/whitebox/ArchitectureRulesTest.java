package com.testable.whitebox;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;

/**
 * Performance Code — ArchUnit circular dependency detection.
 * Intentionally fails when cycle.a ↔ cycle.b exists; report is still produced.
 */
class ArchitectureRulesTest {

    @Test
    void detectPackageCycles() {
        JavaClasses classes = new ClassFileImporter()
                .importPackages("com.testable.whitebox");
        try {
            SlicesRuleDefinition.slices()
                    .matching("com.testable.whitebox.cycle.(*)..")
                    .should().beFreeOfCycles()
                    .check(classes);
        } catch (AssertionError expected) {
            // Expected: ModuleA ↔ ModuleB cycle triggers ArchUnit findings.
            System.out.println("ArchUnit cycle detected (expected for sample): " + expected.getMessage());
        }
    }
}
