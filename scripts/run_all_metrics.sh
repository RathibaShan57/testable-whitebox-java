#!/usr/bin/env bash
# Run ALL Java White Box Primary tools (CK, PMD, CPD, Checkstyle, SpotBugs,
# OWASP Dependency-Check, JaCoCo, PIT, Git) against this monolithic sample.

set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPORTS="${REPORTS_DIR:-reports}"
cd "$ROOT"
mkdir -p "$REPORTS"/{ck,pmd,cpd,checkstyle,spotbugs,dependency-check,jacoco,pit,git}

run() {
  echo ""
  echo "===== $1 ====="
  shift
  "$@" || true
}

run "Maven test + JaCoCo" mvn -q test
run "JaCoCo report" mvn -q jacoco:report
if [[ -d target/site/jacoco ]]; then
  cp -R target/site/jacoco/* "$REPORTS/jacoco/" || true
fi

run "CK" mvn -q dependency:copy@copy-ck
if [[ -f target/tools/ck.jar ]]; then
  java -jar target/tools/ck.jar src/main/java true 0 false "$REPORTS/ck" 1 || true
fi

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

echo ""
echo "ALL JAVA PRIMARY TOOLS EXECUTED — reports in $ROOT/$REPORTS/"
