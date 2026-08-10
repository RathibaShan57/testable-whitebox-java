<#
.SYNOPSIS
    Run Primary tools from Testable_Strategy_Metrics_Mapping_v0.2 against this repo.
.DESCRIPTION
    Sheets covered:
      White Box          - CK, PMD, CPD, Checkstyle, SpotBugs, ODC, JaCoCo, PIT, Git
      Performance Code   - Lizard, PMD/Semgrep AST, SpotBugs concurrency, ArchUnit, Git, JaCoCo
      Security Code      - Gitleaks, detect-secrets, Trufflehog, Checkov/tfsec/kics, GitHub API
      Compliance Code    - Gitleaks/Trufflehog, Semgrep PII, Presidio, Checkov, GitHub API
      Black Box / URL    - SampleApiServer + Playwright, Newman/Postman, OWASP ZAP, k6
#>

param(
    [string]$ReportsDir = "reports",
    [switch]$SkipDependencyCheck,
    [switch]$SkipPit,
    [switch]$SkipDynamic,
    [switch]$FailFast
)

$ErrorActionPreference = "Continue"
$Root = Split-Path -Parent $PSScriptRoot
Push-Location $Root

$dirs = @(
    "ck","pmd","cpd","checkstyle","spotbugs","dependency-check","jacoco","pit","git",
    "lizard","semgrep","gitleaks","trufflehog","detect-secrets","checkov","tfsec","kics",
    "presidio","archunit","github-api","newman","k6","zap","playwright"
)
foreach ($d in $dirs) {
    New-Item -ItemType Directory -Force -Path (Join-Path $ReportsDir $d) | Out-Null
}

function Invoke-MetricTool {
    param([string]$Name, [scriptblock]$Action)
    Write-Host ""
    Write-Host "===== $Name =====" -ForegroundColor Cyan
    & $Action
    if ($LASTEXITCODE -ne 0 -and $FailFast) { Pop-Location; exit $LASTEXITCODE }
}

function Test-ExeAvailable {
    param([string]$Name)
    return [bool](Get-Command $Name -ErrorAction SilentlyContinue)
}

# --- White Box + Performance Code (Maven family) ---
Invoke-MetricTool 'Maven test + JaCoCo prepare-agent' {
    mvn -q -DskipTests=false test jacoco:report verify -Pcoverage-delta
}

Invoke-MetricTool 'PMD java-perf-dependency profile' {
    mvn -q -Pjava-perf-dependency verify -DskipTests
    if (-not (Test-Path "target\pmd.xml")) {
        Write-Host "target/pmd.xml missing after java-perf-dependency profile" -ForegroundColor Red
    }
}

Invoke-MetricTool 'Coverage delta summary' {
    python scripts/run_coverage_delta.py
}

Invoke-MetricTool 'JaCoCo report copy' {
    if (Test-Path "target\site\jacoco") {
        Copy-Item -Recurse -Force "target\site\jacoco\*" (Join-Path $ReportsDir "jacoco\")
    }
}

Invoke-MetricTool 'CK class metrics' {
    mvn -q dependency:copy@copy-ck
    $ckJar = "target\tools\ck.jar"
    if (Test-Path $ckJar) {
        java -jar $ckJar "src\main\java" true 0 false (Join-Path $ReportsDir "ck\")
    } else {
        Write-Host "CK jar missing - skipped" -ForegroundColor Yellow
    }
}

Invoke-MetricTool 'PMD' {
    mvn -q pmd:pmd
    if (Test-Path "target\pmd.xml") {
        Copy-Item "target\pmd.xml" (Join-Path $ReportsDir "pmd\pmd.xml") -Force
    }
}

Invoke-MetricTool 'CPD' {
    mvn -q pmd:cpd
    if (Test-Path "target\cpd.xml") {
        Copy-Item "target\cpd.xml" (Join-Path $ReportsDir "cpd\cpd.xml") -Force
    }
}

Invoke-MetricTool 'Checkstyle' {
    mvn -q checkstyle:checkstyle
    if (Test-Path "target\checkstyle-result.xml") {
        Copy-Item "target\checkstyle-result.xml" (Join-Path $ReportsDir "checkstyle\checkstyle-result.xml") -Force
    }
}

Invoke-MetricTool 'SpotBugs SAST and concurrency' {
    mvn -q spotbugs:spotbugs
    Get-ChildItem "target" -Filter "spotbugs*" -ErrorAction SilentlyContinue |
        ForEach-Object { Copy-Item $_.FullName (Join-Path $ReportsDir "spotbugs\") -Force }
}

if (-not $SkipDependencyCheck) {
    Invoke-MetricTool 'OWASP Dependency-Check' {
        mvn -q org.owasp:dependency-check-maven:check
        if (Test-Path "target\dependency-check-report.xml") {
            Copy-Item "target\dependency-check-report.*" (Join-Path $ReportsDir "dependency-check\") -Force
        } else {
            Write-Host "target/dependency-check-report.xml missing" -ForegroundColor Red
        }
    }
} else {
    Write-Host ""
    Write-Host "===== OWASP Dependency-Check SKIPPED =====" -ForegroundColor Yellow
}

if (-not $SkipPit) {
    Invoke-MetricTool 'PIT mutation testing' {
        mvn -q org.pitest:pitest-maven:mutationCoverage
        if (Test-Path "target\pit-reports") {
            Copy-Item -Recurse -Force "target\pit-reports\*" (Join-Path $ReportsDir "pit\")
        }
    }
} else {
    Write-Host ""
    Write-Host "===== PIT SKIPPED =====" -ForegroundColor Yellow
}

Invoke-MetricTool 'Git churn hotspot analysis' {
    $gitOut = Join-Path $ReportsDir "git"
    git rev-parse --is-inside-work-tree > (Join-Path $gitOut "git_repo.txt") 2>&1
    git log --pretty=format:"%h|%ad|%s" --date=short -n 50 > (Join-Path $gitOut "recent_commits.txt") 2>&1
    git log --numstat --pretty=format:"COMMIT %h %ad" --date=short -n 50 -- "src/**/*.java" > (Join-Path $gitOut "numstat.txt") 2>&1
    git shortlog -sn -n 20 > (Join-Path $gitOut "authors.txt") 2>&1
    git log --name-only --pretty=format: -n 200 -- "src/**/*.java" |
        Where-Object { $_ -ne "" } |
        Group-Object |
        Sort-Object Count -Descending |
        Select-Object -First 30 Count, Name |
        Format-Table -AutoSize |
        Out-String |
        Set-Content (Join-Path $gitOut "hotspots.txt")
}

# --- Performance Code extras ---
Invoke-MetricTool 'Lizard algorithmic complexity' {
    if (Test-ExeAvailable "lizard") {
        lizard src/main/java -l java -C 15 -o (Join-Path $ReportsDir "lizard\lizard.txt")
        lizard src/main/java -l java --csv > (Join-Path $ReportsDir "lizard\lizard.csv")
    } elseif (Test-ExeAvailable "python") {
        python -m pip install --quiet lizard 2>$null
        python -m lizard src/main/java -l java -C 15 -o (Join-Path $ReportsDir "lizard\lizard.txt")
    } else {
        Write-Host "lizard not available - skipped" -ForegroundColor Yellow
    }
}

Invoke-MetricTool 'Semgrep N+1 PII alloc-in-loop' {
    if (Test-ExeAvailable "semgrep") {
        semgrep --config .semgrep/testable-qa.yml --json -o (Join-Path $ReportsDir "semgrep\semgrep.json") src/main/java
        semgrep --config .semgrep/testable-qa.yml -o (Join-Path $ReportsDir "semgrep\semgrep.txt") src/main/java
    } elseif (Test-ExeAvailable "python") {
        python -m pip install --quiet semgrep 2>$null
        semgrep --config .semgrep/testable-qa.yml --json -o (Join-Path $ReportsDir "semgrep\semgrep.json") src/main/java
    } else {
        Write-Host "semgrep not available - skipped" -ForegroundColor Yellow
    }
}

Invoke-MetricTool 'ArchUnit cycle report' {
    if (Test-Path "target\surefire-reports") {
        Copy-Item -Recurse -Force "target\surefire-reports\*" (Join-Path $ReportsDir "archunit\") -ErrorAction SilentlyContinue
    }
    "ArchUnit executed via ArchitectureRulesTest during mvn test" |
        Set-Content (Join-Path $ReportsDir "archunit\README.txt")
}

# --- Security / Compliance Code ---
Invoke-MetricTool 'Gitleaks secret scanning' {
    $outJson = Join-Path $ReportsDir "gitleaks\gitleaks.json"
    if (Test-ExeAvailable "gitleaks") {
        gitleaks detect --source . --config config/gitleaks.toml --report-path $outJson --report-format json --no-git 2>(Join-Path $ReportsDir "gitleaks\gitleaks.log")
        gitleaks detect --source . --config config/gitleaks.toml --report-path (Join-Path $ReportsDir "gitleaks\gitleaks-history.json") --report-format json 2>> (Join-Path $ReportsDir "gitleaks\gitleaks.log")
    } elseif (Test-ExeAvailable "docker") {
        docker run --rm -v "${Root}:/repo" zricethezav/gitleaks:latest detect --source=/repo --no-git -f json -r "/repo/$ReportsDir/gitleaks/gitleaks.json"
    } else {
        Write-Host "gitleaks not available - skipped" -ForegroundColor Yellow
    }
}

Invoke-MetricTool 'detect-secrets' {
    $out = Join-Path $ReportsDir "detect-secrets\baseline.json"
    if (Test-ExeAvailable "detect-secrets") {
        detect-secrets scan --all-files > $out
    } elseif (Test-ExeAvailable "python") {
        python -m pip install --quiet detect-secrets 2>$null
        detect-secrets scan --all-files > $out
    } else {
        Write-Host "detect-secrets not available - skipped" -ForegroundColor Yellow
    }
}

Invoke-MetricTool 'Trufflehog git history secrets' {
    & (Join-Path $Root "scripts\seed_trufflehog_git_history.ps1")
    $out = Join-Path $ReportsDir "trufflehog\trufflehog-git.json"
    if (Test-ExeAvailable "trufflehog") {
        trufflehog git file://. --json > $out 2>(Join-Path $ReportsDir "trufflehog\trufflehog-git.log")
    } elseif (Test-ExeAvailable "docker") {
        docker run --rm -v "${Root}:/repo" trufflesecurity/trufflehog:latest git file:///repo --json > $out
    } else {
        Write-Host "trufflehog not available - skipped" -ForegroundColor Yellow
    }
}

Invoke-MetricTool 'Trufflehog filesystem secrets' {
    $out = Join-Path $ReportsDir "trufflehog\trufflehog.json"
    if (Test-ExeAvailable "trufflehog") {
        trufflehog filesystem . --json > $out 2>(Join-Path $ReportsDir "trufflehog\trufflehog.log")
    } elseif (Test-ExeAvailable "docker") {
        docker run --rm -v "${Root}:/repo" trufflesecurity/trufflehog:latest filesystem /repo --json > $out
    } else {
        Write-Host "trufflehog not available - skipped" -ForegroundColor Yellow
    }
}

Invoke-MetricTool 'Checkov IaC' {
    if (Test-ExeAvailable "checkov") {
        checkov -d infra/terraform -o json --output-file-path (Join-Path $ReportsDir "checkov") --soft-fail
    } elseif (Test-ExeAvailable "python") {
        python -m pip install --quiet checkov 2>$null
        checkov -d infra/terraform -o json --output-file-path (Join-Path $ReportsDir "checkov") --soft-fail
    } elseif (Test-ExeAvailable "docker") {
        docker run --rm -v "${Root}:/repo" bridgecrew/checkov -d /repo/infra/terraform -o json --soft-fail > (Join-Path $ReportsDir "checkov\checkov.json")
    } else {
        Write-Host "checkov not available - skipped" -ForegroundColor Yellow
    }
}

Invoke-MetricTool 'tfsec IaC' {
    if (Test-ExeAvailable "tfsec") {
        tfsec infra/terraform --format json --out (Join-Path $ReportsDir "tfsec\tfsec.json")
    } elseif (Test-ExeAvailable "docker") {
        docker run --rm -v "${Root}:/repo" aquasec/tfsec /repo/infra/terraform --format json > (Join-Path $ReportsDir "tfsec\tfsec.json")
    } else {
        Write-Host "tfsec not available - skipped" -ForegroundColor Yellow
    }
}

Invoke-MetricTool 'kics IaC' {
    if (Test-ExeAvailable "kics") {
        kics scan -p infra/terraform -o (Join-Path $ReportsDir "kics") --silent
    } elseif (Test-ExeAvailable "docker") {
        docker run --rm -v "${Root}:/repo" checkmarx/kics:latest scan -p /repo/infra/terraform -o "/repo/$ReportsDir/kics"
    } else {
        Write-Host "kics not available - skipped" -ForegroundColor Yellow
    }
}

Invoke-MetricTool 'Presidio PII in fixtures' {
    if (Test-ExeAvailable "python") {
        python -m pip install --quiet presidio-analyzer 2>$null
        python scripts/run_presidio.py --input fixtures/pii --output (Join-Path $ReportsDir "presidio\presidio.json")
    } else {
        Write-Host "python/presidio not available - skipped" -ForegroundColor Yellow
    }
}

Invoke-MetricTool 'GitHub Branch Protection Access API' {
    if (Test-ExeAvailable "gh") {
        $repo = gh repo view --json nameWithOwner -q .nameWithOwner 2>$null
        if ($repo) {
            gh api "repos/$repo/branches/master/protection" > (Join-Path $ReportsDir "github-api\branch-protection.json") 2> (Join-Path $ReportsDir "github-api\branch-protection.err")
            gh api "repos/$repo/collaborators" > (Join-Path $ReportsDir "github-api\collaborators.json") 2> (Join-Path $ReportsDir "github-api\collaborators.err")
            "repo=$repo" | Set-Content (Join-Path $ReportsDir "github-api\repo.txt")
            Copy-Item (Join-Path $ReportsDir "github-api\collaborators.json") ".testable\github\collaborators.json" -Force -ErrorAction SilentlyContinue
            Copy-Item (Join-Path $ReportsDir "github-api\branch-protection.json") ".testable\github\branch-protection.json" -Force -ErrorAction SilentlyContinue
        } else {
            "gh repo view failed - not a GitHub remote?" | Set-Content (Join-Path $ReportsDir "github-api\skip.txt")
            Copy-Item ".testable\github\collaborators.json" (Join-Path $ReportsDir "github-api\collaborators.json") -Force -ErrorAction SilentlyContinue
        }
    } else {
        Write-Host "gh not available - using .testable/github fallback" -ForegroundColor Yellow
        Copy-Item ".testable\github\collaborators.json" (Join-Path $ReportsDir "github-api\collaborators.json") -Force -ErrorAction SilentlyContinue
    }
}

# --- Black Box / URL dynamic tools ---
if (-not $SkipDynamic) {
    Invoke-MetricTool 'Start SampleApiServer' {
        mvn -q -DskipTests package
        $script:ApiJob = Start-Process -FilePath "java" `
            -ArgumentList "-cp","target/classes","com.testable.whitebox.SampleApiServer","8089" `
            -PassThru -WindowStyle Hidden
        Start-Sleep -Seconds 2
    }

    try {
        Invoke-MetricTool 'Newman Postman API black box' {
            if (Test-ExeAvailable "newman") {
                newman run blackbox/postman/testable-sample.postman_collection.json `
                    --env-var "baseUrl=http://127.0.0.1:8089" `
                    -r 'cli,json' --reporter-json-export (Join-Path $ReportsDir "newman\newman.json")
            } elseif (Test-ExeAvailable "npx") {
                npx --yes newman run blackbox/postman/testable-sample.postman_collection.json `
                    --env-var "baseUrl=http://127.0.0.1:8089" `
                    -r 'cli,json' --reporter-json-export (Join-Path $ReportsDir "newman\newman.json")
            } else {
                Write-Host "newman/npx not available - skipped" -ForegroundColor Yellow
            }
        }

        Invoke-MetricTool 'k6 load test' {
            if (Test-ExeAvailable "k6") {
                $env:BASE_URL = "http://127.0.0.1:8089"
                k6 run blackbox/k6/load.js --summary-export=(Join-Path $ReportsDir "k6\summary.json")
            } else {
                Write-Host "k6 not available - skipped" -ForegroundColor Yellow
            }
        }

        Invoke-MetricTool 'Playwright smoke' {
            if (Test-ExeAvailable "npx") {
                Push-Location blackbox/playwright
                try {
                    npm install --silent 2>$null
                    npx playwright install chromium 2>$null
                    $env:BASE_URL = "http://127.0.0.1:8089"
                    npx playwright test --reporter=list 2>&1 | Tee-Object (Join-Path $Root "$ReportsDir\playwright\playwright.log")
                } finally {
                    Pop-Location
                }
            } else {
                Write-Host "npx not available - skipped" -ForegroundColor Yellow
            }
        }

        Invoke-MetricTool 'OWASP ZAP baseline docker' {
            if (Test-ExeAvailable "docker") {
                $zapDir = Join-Path $Root (Join-Path $ReportsDir "zap")
                docker run --rm --add-host=host.docker.internal:host-gateway `
                    -v "${zapDir}:/zap/wrk" `
                    ghcr.io/zaproxy/zaproxy:stable zap-baseline.py `
                    -t http://host.docker.internal:8089 -J zap-report.json -w zap-report.md
            } else {
                Write-Host "docker not available for ZAP - skipped" -ForegroundColor Yellow
            }
        }
    } finally {
        if ($script:ApiJob -and -not $script:ApiJob.HasExited) {
            Stop-Process -Id $script:ApiJob.Id -Force -ErrorAction SilentlyContinue
        }
    }
} else {
    Write-Host ""
    Write-Host "===== Dynamic URL/Black-Box tools SKIPPED (-SkipDynamic) =====" -ForegroundColor Yellow
}

Pop-Location

Write-Host ""
Write-Host "ALL MAPPED PRIMARY TOOLS EXECUTED (available locally)" -ForegroundColor Green
Write-Host "Reports: $Root\$ReportsDir\" -ForegroundColor Green
Write-Host "White Box          : ck pmd cpd checkstyle spotbugs odc jacoco pit git"
Write-Host "Performance Code   : lizard semgrep archunit spotbugs jacoco git"
Write-Host "Security/Compliance: gitleaks detect-secrets trufflehog checkov tfsec kics presidio github-api"
Write-Host "Black Box / URL    : newman k6 playwright zap (+ SampleApiServer)"
