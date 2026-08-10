<#
.SYNOPSIS
    Run ALL Java White Box Primary tools against this monolithic sample repo.
.DESCRIPTION
    Tools from Testable_Strategy_Metrics_Mapping_v0.2 (Java Primary column):
      CK, PMD, CPD, Checkstyle, SpotBugs, OWASP Dependency-Check, JaCoCo, PIT, Git
#>

param(
    [string]$ReportsDir = "reports",
    [switch]$SkipDependencyCheck,
    [switch]$SkipPit,
    [switch]$FailFast
)

$ErrorActionPreference = "Continue"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root

New-Item -ItemType Directory -Force -Path $ReportsDir | Out-Null
New-Item -ItemType Directory -Force -Path "$ReportsDir\ck" | Out-Null
New-Item -ItemType Directory -Force -Path "$ReportsDir\pmd" | Out-Null
New-Item -ItemType Directory -Force -Path "$ReportsDir\cpd" | Out-Null
New-Item -ItemType Directory -Force -Path "$ReportsDir\checkstyle" | Out-Null
New-Item -ItemType Directory -Force -Path "$ReportsDir\spotbugs" | Out-Null
New-Item -ItemType Directory -Force -Path "$ReportsDir\dependency-check" | Out-Null
New-Item -ItemType Directory -Force -Path "$ReportsDir\jacoco" | Out-Null
New-Item -ItemType Directory -Force -Path "$ReportsDir\pit" | Out-Null
New-Item -ItemType Directory -Force -Path "$ReportsDir\git" | Out-Null

function Run-Tool {
    param([string]$Name, [scriptblock]$Action)
    Write-Host "`n===== $Name =====" -ForegroundColor Cyan
    & $Action
    if ($LASTEXITCODE -ne 0 -and $FailFast) { Pop-Location; exit $LASTEXITCODE }
}

# 1. Compile + unit tests + JaCoCo agent
Run-Tool "Maven test + JaCoCo prepare-agent" {
    mvn -q -DskipTests=false test
}

# 2. JaCoCo report (statement / branch / path proxy)
Run-Tool "JaCoCo report" {
    mvn -q jacoco:report
    if (Test-Path "target\site\jacoco") {
        Copy-Item -Recurse -Force "target\site\jacoco\*" "$ReportsDir\jacoco\"
    }
}

# 3. CK — cyclomatic / WMC / coupling
Run-Tool "CK (class metrics)" {
    mvn -q dependency:copy@copy-ck
    $ckJar = "target\tools\ck.jar"
    if (Test-Path $ckJar) {
        java -jar $ckJar "src\main\java" true 0 false "$ReportsDir\ck" 1
    } else {
        Write-Host "CK jar missing — skipped" -ForegroundColor Yellow
    }
}

# 4. PMD — cognitive complexity / design / security rules
Run-Tool "PMD" {
    mvn -q pmd:pmd
    if (Test-Path "target\pmd.xml") { Copy-Item "target\pmd.xml" "$ReportsDir\pmd\pmd.xml" -Force }
    if (Test-Path "target\site\pmd.html") { Copy-Item "target\site\pmd.html" "$ReportsDir\pmd\pmd.html" -Force }
}

# 5. CPD — code duplication
Run-Tool "CPD (PMD Copy/Paste Detector)" {
    mvn -q pmd:cpd
    if (Test-Path "target\cpd.xml") { Copy-Item "target\cpd.xml" "$ReportsDir\cpd\cpd.xml" -Force }
    if (Test-Path "target\site\cpd.html") { Copy-Item "target\site\cpd.html" "$ReportsDir\cpd\cpd.html" -Force }
}

# 6. Checkstyle — lint / rule violations
Run-Tool "Checkstyle" {
    mvn -q checkstyle:checkstyle
    if (Test-Path "target\checkstyle-result.xml") {
        Copy-Item "target\checkstyle-result.xml" "$ReportsDir\checkstyle\checkstyle-result.xml" -Force
    }
}

# 7. SpotBugs (+ FindSecBugs) — SAST
Run-Tool "SpotBugs SAST" {
    mvn -q spotbugs:spotbugs spotbugs:gui -DskipTests 2>$null
    mvn -q spotbugs:spotbugs
    if (Test-Path "target\spotbugsXml.xml") {
        Copy-Item "target\spotbugsXml.xml" "$ReportsDir\spotbugs\spotbugsXml.xml" -Force
    }
    Get-ChildItem "target" -Filter "spotbugs*.xml" -ErrorAction SilentlyContinue |
        ForEach-Object { Copy-Item $_.FullName "$ReportsDir\spotbugs\" -Force }
    Get-ChildItem "target" -Filter "spotbugs*.html" -ErrorAction SilentlyContinue |
        ForEach-Object { Copy-Item $_.FullName "$ReportsDir\spotbugs\" -Force }
}

# 8. OWASP Dependency-Check — SCA / Known CVE Count
if (-not $SkipDependencyCheck) {
    Run-Tool "OWASP Dependency-Check" {
        mvn -q org.owasp:dependency-check-maven:check
    }
} else {
    Write-Host "`n===== OWASP Dependency-Check SKIPPED (-SkipDependencyCheck) =====" -ForegroundColor Yellow
}

# 9. PIT — mutation testing
if (-not $SkipPit) {
    Run-Tool "PIT mutation testing" {
        mvn -q org.pitest:pitest-maven:mutationCoverage
    }
} else {
    Write-Host "`n===== PIT SKIPPED (-SkipPit) =====" -ForegroundColor Yellow
}

# 10. Git — code churn / hotspot proxies
Run-Tool "Git churn / hotspot analysis" {
    $gitOut = "$ReportsDir\git"
    git rev-parse --is-inside-work-tree > "$gitOut\git_repo.txt" 2>&1
    git log --pretty=format:"%h|%ad|%s" --date=short -n 50 > "$gitOut\recent_commits.txt" 2>&1
    git log --numstat --pretty=format:"COMMIT %h %ad" --date=short -n 50 -- "src/**/*.java" > "$gitOut\numstat.txt" 2>&1
    git shortlog -sn -n 20 > "$gitOut\authors.txt" 2>&1
    # Hotspot proxy: files touched most often
    git log --name-only --pretty=format: -n 200 -- "src/**/*.java" |
        Where-Object { $_ -ne "" } |
        Group-Object |
        Sort-Object Count -Descending |
        Select-Object -First 30 Count, Name |
        Format-Table -AutoSize |
        Out-String |
        Set-Content "$gitOut\hotspots.txt"
}

Pop-Location

Write-Host "`n═══════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host "  ALL JAVA PRIMARY TOOLS EXECUTED" -ForegroundColor Green
Write-Host "  Reports: $Root\$ReportsDir\" -ForegroundColor Green
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Green
Write-Host "  CK                 : $ReportsDir\ck\"
Write-Host "  PMD                : $ReportsDir\pmd\"
Write-Host "  CPD                : $ReportsDir\cpd\"
Write-Host "  Checkstyle         : $ReportsDir\checkstyle\"
Write-Host "  SpotBugs           : $ReportsDir\spotbugs\"
Write-Host "  Dependency-Check   : $ReportsDir\dependency-check\"
Write-Host "  JaCoCo             : $ReportsDir\jacoco\"
Write-Host "  PIT                : $ReportsDir\pit\"
Write-Host "  Git churn          : $ReportsDir\git\"
