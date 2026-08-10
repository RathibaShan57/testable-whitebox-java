package com.testable.whitebox;

/**
 * Calculator — triggers CK (WMC / CBO), JaCoCo statement/branch coverage,
 * PIT mutation score, and PMD cyclomatic complexity metrics.
 */
public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    /** CC≈3 — two decision points. */
    public double divide(double numerator, double denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        if (Double.isNaN(numerator)) {
            throw new IllegalArgumentException("Numerator must be numeric");
        }
        return numerator / denominator;
    }

    /** Compound predicate for condition / branch coverage. */
    public String classifyScore(int score, boolean isBonus, boolean isPremium) {
        if (score < 0 || score > 100) {
            return "INVALID";
        }
        if (score >= 90 && (isBonus || isPremium)) {
            return "EXCELLENT+";
        }
        if (score >= 75) {
            return "GOOD";
        }
        if (score >= 50) {
            return "AVERAGE";
        }
        return "POOR";
    }

    /** CC≈8 — decision-heavy grading. */
    public String gradeStudent(int marks, int attendance, boolean passedExam) {
        String grade = "F";
        if (attendance < 75) {
            return "DETAINED";
        }
        if (!passedExam) {
            if (marks >= 40) {
                grade = "E";
            } else {
                grade = "F";
            }
        } else if (marks >= 90) {
            grade = "A+";
        } else if (marks >= 80) {
            grade = "A";
        } else if (marks >= 70) {
            grade = "B";
        } else if (marks >= 60) {
            grade = "C";
        } else if (marks >= 50) {
            grade = "D";
        }
        return grade;
    }

    /**
     * High-complexity premium calculator — Technical Debt / QA priority (CK WMC).
     */
    public double computeInsurancePremium(
            int age,
            String gender,
            boolean smoker,
            int yearsLicensed,
            int accidents,
            boolean comprehensive,
            String region,
            double vehicleValue) {

        double base = vehicleValue * 0.02;
        if (age < 25) {
            base *= 1.5;
        } else if (age < 35) {
            base *= 1.2;
        } else if (age > 65) {
            base *= 1.3;
        }

        if ("M".equalsIgnoreCase(gender)) {
            base *= 1.1;
        }

        if (smoker) {
            base *= 1.15;
        }

        if (yearsLicensed < 2) {
            base *= 1.4;
        } else if (yearsLicensed < 5) {
            base *= 1.15;
        }

        if (accidents >= 3) {
            base *= 1.8;
        } else if (accidents == 2) {
            base *= 1.4;
        } else if (accidents == 1) {
            base *= 1.15;
        }

        if (comprehensive) {
            base *= 1.25;
        }

        if ("URBAN".equalsIgnoreCase(region)) {
            base *= 1.2;
        } else if ("COASTAL".equalsIgnoreCase(region)) {
            base *= 1.1;
        }

        if (vehicleValue > 50000) {
            base += 200;
        }

        return Math.round(base * 100.0) / 100.0;
    }
}
