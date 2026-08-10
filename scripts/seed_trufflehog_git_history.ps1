# Seed git history with a removed secret for Trufflehog git-history metrics.
$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Bait = Join-Path $Root 'fixtures\secrets\historical-credential.txt'
Set-Location $Root

$history = git log --oneline -- fixtures/secrets/historical-credential.txt 2>$null
if ($history) {
    Write-Host 'Git history bait already present — skipping.'
    git show 'HEAD~1:fixtures/secrets/historical-credential.txt' | Set-Content -Encoding utf8 $Bait
    exit 0
}

@'
# Rotated credential — intentionally removed from HEAD but retained in git history
AWS_ACCESS_KEY_ID=AKIA_HISTORICAL_BAIT_DO_NOT_USE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
'@ | Set-Content -Encoding utf8 $Bait

git add $Bait
git commit -m 'test: add rotated credential fixture for trufflehog git scan'
git rm -f $Bait
git commit -m 'test: remove rotated credential from HEAD (history bait remains)'
New-Item -ItemType Directory -Force (Split-Path $Bait) | Out-Null
git show 'HEAD~1:fixtures/secrets/historical-credential.txt' | Set-Content -Encoding utf8 $Bait
Write-Host 'Seeded trufflehog git-history bait across last 2 commits.'
