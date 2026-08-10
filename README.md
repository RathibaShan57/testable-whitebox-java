# Testable WhiteBox Java Sample Repository

> **Purpose:** A **monolithic, single-language, single-version** Java repository that triggers all **Java Primary tools** from `Testable_Strategy_Metrics_Mapping_v0.2` (White Box sheet).

| Constraint | Value |
|---|---|
| Language | Java only |
| Version | **17** (within allowed range 1.8–21) |
| Layout | One Maven module (monolith) |

---

## Quick Start

```powershell
cd C:\Users\Rathiba\Source\Java\testable-whitebox-java

# Compile + unit tests
mvn test

# Run ALL primary tools (Windows)
.\scripts\run_all_metrics.ps1

# Faster local loop (skip NVD download + mutation)
.\scripts\run_all_metrics.ps1 -SkipDependencyCheck -SkipPit
```

```bash
# Linux / macOS / CI
bash scripts/run_all_metrics.sh
SKIP_DEPENDENCY_CHECK=1 SKIP_PIT=1 bash scripts/run_all_metrics.sh
```

Reports land in `reports/`.

---

## Java Primary Tools Triggered

Mapped from the Excel **White Box → Java → Primary** column (normalized unique set):

| # | Primary Tool | Excel techniques covered | How this repo triggers it |
|---|---|---|---|
| 1 | **CK** | Cyclomatic Complexity / WMC / CBO / LCOM | `ComplexLogic`, `Calculator` high WMC; run via `ck.jar` |
| 2 | **PMD** | Cognitive Complexity, design / security rules | Nested scheduler + custom `config/pmd-ruleset.xml` |
| 3 | **CPD** | Code Duplication | Intentional clones in `DuplicatedCode` |
| 4 | **Checkstyle** | Lint / Rule Violations | Naming, unused locals, long lines, complexity in `Utils` |
| 5 | **SpotBugs** (+ FindSecBugs) | SAST | SQL injection, weak MD5/SHA-1, hardcoded secrets, insecure deserialisation in `AuthService` |
| 6 | **OWASP Dependency-Check** | SCA / CVE / license proxies | Pinned vulnerable deps: commons-collections 3.2.1, jackson-databind 2.9.10, log4j-core 2.14.1 |
| 7 | **JaCoCo** | Statement / Branch / Path coverage proxies + coverage delta inputs | JUnit 5 suite + `jacoco-maven-plugin` XML/HTML |
| 8 | **PIT** | Mutation Score | `pitest-maven` over `com.testable.whitebox.*` |
| 9 | **Git** | Code churn / hotspots / regression focus | `scripts/run_all_metrics.*` emit numstat + hotspot reports |

> Note: Some Excel cells list composite strings such as “JaCoCo + Git + diff-coverage”. Those still resolve to the same Primary tool families above (JaCoCo / Git).

---

## Repository Structure

```
testable-whitebox-java/
├── src/main/java/com/testable/whitebox/
│   ├── Calculator.java       # CK WMC, JaCoCo branches, PIT
│   ├── ComplexLogic.java     # High CC / Cognitive Complexity (CK + PMD)
│   ├── AuthService.java      # SpotBugs / FindSecBugs SAST
│   ├── DuplicatedCode.java   # CPD clones
│   ├── DataProcessor.java    # Data-flow / null paths (SpotBugs + JaCoCo)
│   ├── FileHandler.java      # Exception / branch coverage
│   ├── ApiClient.java        # Vulnerable dependency usage (ODC)
│   └── Utils.java            # Checkstyle / PMD lint density
├── src/test/java/...         # JUnit 5 tests for JaCoCo + PIT
├── config/
│   ├── checkstyle.xml
│   └── pmd-ruleset.xml
├── scripts/
│   ├── run_all_metrics.ps1
│   └── run_all_metrics.sh
├── .github/workflows/whitebox-metrics.yml
└── pom.xml                   # Java 17 + all tool plugins
```

---

## Individual Maven Goals

```bash
mvn test jacoco:report
mvn pmd:pmd pmd:cpd
mvn checkstyle:checkstyle
mvn spotbugs:spotbugs
mvn org.owasp:dependency-check-maven:check
mvn org.pitest:pitest-maven:mutationCoverage
mvn dependency:copy@copy-ck
java -jar target/tools/ck.jar src/main/java true 0 false reports/ck 1
```

---

## Source Mapping (mirrors Python sample intent)

| Source | Primary signals |
|---|---|
| `Calculator` / `ComplexLogic` | CK complexity, JaCoCo paths, PIT kill score |
| `DuplicatedCode` | CPD duplication % |
| `Utils` | Checkstyle + PMD violation density |
| `AuthService` | SpotBugs SAST findings |
| `ApiClient` + old deps in `pom.xml` | OWASP Dependency-Check CVEs |
| Git history on `src/**` | Churn / hotspot proxies |
