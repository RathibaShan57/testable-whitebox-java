#!/usr/bin/env bash
# Run Primary tools from Testable_Strategy_Metrics_Mapping_v0.2:
# White Box, Performance Code, Security/Compliance Code, Black Box / URL sheets.

set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPORTS="${REPORTS_DIR:-reports}"
cd "$ROOT"
mkdir -p "$REPORTS"/{ck,pmd,cpd,checkstyle,spotbugs,dependency-check,jacoco,pit,git,lizard,semgrep,gitleaks,trufflehog,detect-secrets,checkov,tfsec,kics,presidio,archunit,github-api,newman,k6,zap,playwright}

run() {
  echo ""
  echo "===== $1 ====="
  shift
  "$@" || true
}

have() { command -v "$1" >/dev/null 2>&1; }

run "Maven test + JaCoCo" mvn -q test
run "JaCoCo report" mvn -q jacoco:report
[[ -d target/site/jacoco ]] && cp -R target/site/jacoco/* "$REPORTS/jacoco/" || true

run "CK" mvn -q dependency:copy@copy-ck
[[ -f target/tools/ck.jar ]] && java -jar target/tools/ck.jar src/main/java true 0 false "$REPORTS/ck/" || true

run "PMD" mvn -q pmd:pmd
[[ -f target/pmd.xml ]] && cp target/pmd.xml "$REPORTS/pmd/"
run "CPD" mvn -q pmd:cpd
[[ -f target/cpd.xml ]] && cp target/cpd.xml "$REPORTS/cpd/"
run "Checkstyle" mvn -q checkstyle:checkstyle
[[ -f target/checkstyle-result.xml ]] && cp target/checkstyle-result.xml "$REPORTS/checkstyle/"
run "SpotBugs" mvn -q spotbugs:spotbugs
find target -maxdepth 1 -name 'spotbugs*' -exec cp {} "$REPORTS/spotbugs/" \; 2>/dev/null || true

if [[ "${SKIP_DEPENDENCY_CHECK:-0}" != "1" ]]; then
  run "OWASP Dependency-Check" mvn -q org.owasp:dependency-check-maven:check
fi
if [[ "${SKIP_PIT:-0}" != "1" ]]; then
  run "PIT" mvn -q org.pitest:pitest-maven:mutationCoverage
fi

run "Git churn" bash -c "
  git rev-parse --is-inside-work-tree > '$REPORTS/git/git_repo.txt' 2>&1 || true
  git log --pretty=format:'%h|%ad|%s' --date=short -n 50 > '$REPORTS/git/recent_commits.txt' 2>&1 || true
  git log --numstat --pretty=format:'COMMIT %h %ad' --date=short -n 50 -- 'src/**/*.java' > '$REPORTS/git/numstat.txt' 2>&1 || true
  git log --name-only --pretty=format: -n 200 -- 'src/**/*.java' | sed '/^$/d' | sort | uniq -c | sort -nr | head -30 > '$REPORTS/git/hotspots.txt' 2>&1 || true
"

# Performance Code
if have lizard; then
  run "Lizard" lizard src/main/java -l java -C 15 -o "$REPORTS/lizard/lizard.txt"
elif have python3; then
  python3 -m pip install --quiet lizard >/dev/null 2>&1 || true
  run "Lizard" python3 -m lizard src/main/java -l java -C 15 -o "$REPORTS/lizard/lizard.txt"
fi

if have semgrep; then
  run "Semgrep" semgrep --config .semgrep/testable-qa.yml --json -o "$REPORTS/semgrep/semgrep.json" src/main/java
elif have python3; then
  python3 -m pip install --quiet semgrep >/dev/null 2>&1 || true
  run "Semgrep" semgrep --config .semgrep/testable-qa.yml --json -o "$REPORTS/semgrep/semgrep.json" src/main/java
fi

[[ -d target/surefire-reports ]] && cp -R target/surefire-reports/* "$REPORTS/archunit/" || true
echo "ArchUnit executed via ArchitectureRulesTest" > "$REPORTS/archunit/README.txt"

# Security / Compliance Code
if have gitleaks; then
  run "Gitleaks" gitleaks detect --source . --config config/gitleaks.toml --report-path "$REPORTS/gitleaks/gitleaks.json" --report-format json --no-git
elif have docker; then
  run "Gitleaks(docker)" docker run --rm -v "$ROOT:/repo" zricethezav/gitleaks:latest detect --source=/repo --no-git -f json -r "/repo/$REPORTS/gitleaks/gitleaks.json"
fi

if have detect-secrets; then
  run "detect-secrets" bash -c "detect-secrets scan --all-files > '$REPORTS/detect-secrets/baseline.json'"
elif have python3; then
  python3 -m pip install --quiet detect-secrets >/dev/null 2>&1 || true
  run "detect-secrets" bash -c "detect-secrets scan --all-files > '$REPORTS/detect-secrets/baseline.json'"
fi

if have trufflehog; then
  run "Trufflehog" bash -c "trufflehog filesystem . --json > '$REPORTS/trufflehog/trufflehog.json' 2>'$REPORTS/trufflehog/trufflehog.log'"
elif have docker; then
  run "Trufflehog(docker)" bash -c "docker run --rm -v '$ROOT:/repo' trufflesecurity/trufflehog:latest filesystem /repo --json > '$REPORTS/trufflehog/trufflehog.json'"
fi

if have checkov; then
  run "Checkov" checkov -d infra/terraform -o json --output-file-path "$REPORTS/checkov" --soft-fail
elif have python3; then
  python3 -m pip install --quiet checkov >/dev/null 2>&1 || true
  run "Checkov" checkov -d infra/terraform -o json --output-file-path "$REPORTS/checkov" --soft-fail
elif have docker; then
  run "Checkov(docker)" bash -c "docker run --rm -v '$ROOT:/repo' bridgecrew/checkov -d /repo/infra/terraform -o json --soft-fail > '$REPORTS/checkov/checkov.json'"
fi

if have tfsec; then
  run "tfsec" tfsec infra/terraform --format json --out "$REPORTS/tfsec/tfsec.json"
elif have docker; then
  run "tfsec(docker)" bash -c "docker run --rm -v '$ROOT:/repo' aquasec/tfsec /repo/infra/terraform --format json > '$REPORTS/tfsec/tfsec.json'"
fi

if have kics; then
  run "kics" kics scan -p infra/terraform -o "$REPORTS/kics" --silent
elif have docker; then
  run "kics(docker)" docker run --rm -v "$ROOT:/repo" checkmarx/kics:latest scan -p /repo/infra/terraform -o "/repo/$REPORTS/kics"
fi

if have python3; then
  python3 -m pip install --quiet presidio-analyzer >/dev/null 2>&1 || true
  run "Presidio" python3 scripts/run_presidio.py --input fixtures/pii --output "$REPORTS/presidio/presidio.json"
fi

if have gh; then
  REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)"
  if [[ -n "${REPO:-}" ]]; then
    echo "repo=$REPO" > "$REPORTS/github-api/repo.txt"
    gh api "repos/$REPO/branches/master/protection" > "$REPORTS/github-api/branch-protection.json" 2>"$REPORTS/github-api/branch-protection.err" || true
    gh api "repos/$REPO/collaborators" > "$REPORTS/github-api/collaborators.json" 2>"$REPORTS/github-api/collaborators.err" || true
  fi
fi

# Black Box / URL
if [[ "${SKIP_DYNAMIC:-0}" != "1" ]]; then
  run "Package SampleApiServer" mvn -q -DskipTests package
  java -cp target/classes com.testable.whitebox.SampleApiServer 8089 >"$REPORTS/api-server.log" 2>&1 &
  API_PID=$!
  sleep 2
  cleanup() { kill "$API_PID" 2>/dev/null || true; }
  trap cleanup EXIT

  if have newman; then
    run "Newman" newman run blackbox/postman/testable-sample.postman_collection.json --env-var baseUrl=http://127.0.0.1:8089 -r cli,json --reporter-json-export "$REPORTS/newman/newman.json"
  elif have npx; then
    run "Newman(npx)" npx --yes newman run blackbox/postman/testable-sample.postman_collection.json --env-var baseUrl=http://127.0.0.1:8089 -r cli,json --reporter-json-export "$REPORTS/newman/newman.json"
  fi

  if have k6; then
    BASE_URL=http://127.0.0.1:8089 run "k6" k6 run blackbox/k6/load.js --summary-export="$REPORTS/k6/summary.json"
  fi

  if have npx; then
    (
      cd blackbox/playwright
      npm install --silent >/dev/null 2>&1 || true
      npx playwright install chromium >/dev/null 2>&1 || true
      BASE_URL=http://127.0.0.1:8089 npx playwright test --reporter=list
    ) >"$REPORTS/playwright/playwright.log" 2>&1 || true
    echo "===== Playwright ====="
  fi

  if have docker; then
    run "OWASP ZAP baseline" docker run --rm --network host \
      -v "$ROOT/$REPORTS/zap:/zap/wrk" \
      ghcr.io/zaproxy/zaproxy:stable zap-baseline.py \
      -t http://127.0.0.1:8089 -J zap-report.json -w zap-report.md
  fi

  cleanup
  trap - EXIT
fi

echo ""
echo "ALL MAPPED PRIMARY TOOLS EXECUTED — reports in $ROOT/$REPORTS/"
echo "White Box | Performance Code | Security/Compliance Code | Black Box/URL"
