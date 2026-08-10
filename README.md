# Testable Metrics Java Sample Repository

> **Purpose:** A **monolithic, single-language (Java 17)** repository that triggers Primary tools from `Testable_Strategy_Metrics_Mapping_v0.2` — not only the **White Box** sheet, but also **Performance Code**, **Security/Compliance Code**, and runnable stubs for **Black Box / URL** sheets.

| Constraint | Value |
|---|---|
| Language | Java only (plus scanner CLIs / Node for Playwright & Newman) |
| Version | **17** (within allowed range 1.8–21) |
| Layout | One Maven module (monolith) + `SampleApiServer` for dynamic tools |

---

## Quick Start

```powershell
cd C:\Users\Rathiba\Source\Java\testable-whitebox-java

# Compile + unit tests
mvn test

# Run ALL mapped primary tools (Windows)
.\scripts\run_all_metrics.ps1

# Faster local loop
.\scripts\run_all_metrics.ps1 -SkipDependencyCheck -SkipPit -SkipDynamic
```

```bash
bash scripts/run_all_metrics.sh
SKIP_DEPENDENCY_CHECK=1 SKIP_PIT=1 SKIP_DYNAMIC=1 bash scripts/run_all_metrics.sh
```

Reports land in `reports/`. Tools that are not installed are skipped with a warning (CI installs the common ones).

---

## Sheet → Tool Coverage

### White Box (Java Primary)

| Tool | How triggered |
|---|---|
| **CK** | `ComplexLogic` / `Calculator` + `ck.jar` |
| **PMD** / **CPD** | Nested scheduler + `DuplicatedCode` |
| **Checkstyle** | Naming / complexity in `Utils` |
| **SpotBugs** (+ FindSecBugs) | `AuthService` SAST patterns |
| **OWASP Dependency-Check** | Pinned vulnerable deps in `pom.xml` |
| **JaCoCo** | JUnit 5 suite |
| **PIT** | `pitest-maven` |
| **Git** | churn / hotspot scripts |

### Performance Code (Repository) — Java Primary

| Tool | How triggered |
|---|---|
| **Lizard** | `PerformanceHotspots` (CC / nesting) |
| **PMD AST** | Nested loops + unused-import rules (existing PMD rulesets) |
| **Semgrep** (Hibernate / alloc) | `OrderRepository` N+1 + alloc-in-loop rules in `.semgrep/` |
| **SpotBugs concurrency** | `RaceyCounter` unsynchronized shared state |
| **ArchUnit** | `cycle.ModuleA` ↔ `ModuleB` |
| **GitHub Actions** | `.github/workflows/whitebox-metrics.yml` (build duration) |
| **git log** | same churn reports as White Box |
| **JaCoCo** | coverage on performance-critical classes |

### Security Code + Compliance Code (Repository)

| Tool | How triggered |
|---|---|
| **Gitleaks** / **Trufflehog** / **detect-secrets** | Hardcoded keys in `AuthService` + scan of tree/history |
| **Checkov** / **tfsec** / **kics** | Intentional misconfig in `infra/terraform/main.tf` |
| **Semgrep (custom)** | `PiiLogger` PII-in-logs rule |
| **Microsoft Presidio** | `fixtures/pii/students.csv` via `scripts/run_presidio.py` |
| **GitHub Branch Protection / Collaborators API** | `gh api` in scripts + CI |

### Black Box + Security/Compliance/Performance URL (need live app)

`SampleApiServer` (`http://127.0.0.1:8089`) exposes weak auth, open CORS, and PII endpoints so dynamic tools have a target:

| Tool | Artifact |
|---|---|
| **Playwright** | `blackbox/playwright/` |
| **Newman / Postman** | `blackbox/postman/testable-sample.postman_collection.json` |
| **k6** | `blackbox/k6/load.js` |
| **OWASP ZAP** | docker `zap-baseline.py` against SampleApiServer |
| **OpenAPI / Pact-style contract** | `openapi/openapi.yaml` + `SampleApiContractTest` |

**Not auto-run (commercial / env-specific):** 42Crunch, Drata, OneTrust, mitmproxy, testssl.sh, Redis/Prometheus/DB metric backends. OpenAPI + placeholders are present where useful; wire those vendors in your environment as needed.

---

## Repository Structure

```
testable-whitebox-java/
├── src/main/java/com/testable/whitebox/
│   ├── Calculator.java / ComplexLogic.java / Utils.java / DuplicatedCode.java
│   ├── AuthService.java          # SpotBugs SAST + secret scanners
│   ├── PerformanceHotspots.java  # Lizard / PMD / Semgrep alloc
│   ├── OrderRepository.java      # N+1 Semgrep
│   ├── RaceyCounter.java         # SpotBugs concurrency
│   ├── PiiLogger.java            # Semgrep PII logs
│   ├── SampleApiServer.java      # Black Box / URL target
│   └── cycle/a|b/Module*.java    # ArchUnit package cycles
├── .semgrep/testable-qa.yml
├── infra/terraform/main.tf       # Checkov / tfsec / kics bait
├── fixtures/pii/                 # Presidio
├── blackbox/{postman,k6,playwright}
├── openapi/openapi.yaml
├── scripts/run_all_metrics.{ps1,sh}
└── .github/workflows/whitebox-metrics.yml
```

---

## Individual Commands

```bash
# White Box core
mvn test jacoco:report
mvn pmd:pmd pmd:cpd checkstyle:checkstyle spotbugs:spotbugs
mvn org.owasp:dependency-check-maven:check
mvn org.pitest:pitest-maven:mutationCoverage

# Performance / Compliance extras
lizard src/main/java -l java -C 15
semgrep --config .semgrep/testable-qa.yml src/main/java
checkov -d infra/terraform --soft-fail
gitleaks detect --source . --config config/gitleaks.toml --no-git

# Dynamic target
mvn -q -DskipTests package
java -cp target/classes com.testable.whitebox.SampleApiServer 8089
newman run blackbox/postman/testable-sample.postman_collection.json --env-var baseUrl=http://127.0.0.1:8089
BASE_URL=http://127.0.0.1:8089 k6 run blackbox/k6/load.js
```
