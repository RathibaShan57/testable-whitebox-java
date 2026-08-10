#!/usr/bin/env bash
# Seed git history with a removed secret for Trufflehog git-history metrics.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BAIT="$ROOT/fixtures/secrets/historical-credential.txt"
cd "$ROOT"

if git log --oneline -- fixtures/secrets/historical-credential.txt 2>/dev/null | grep -q .; then
  echo "Git history bait already present — skipping."
  git show "HEAD~1:fixtures/secrets/historical-credential.txt" > "$BAIT" 2>/dev/null || true
  exit 0
fi

cat > "$BAIT" <<'EOF'
# Rotated credential — intentionally removed from HEAD but retained in git history
AWS_ACCESS_KEY_ID=AKIA_HISTORICAL_BAIT_DO_NOT_USE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
EOF

git add "$BAIT"
git commit -m "test: add rotated credential fixture for trufflehog git scan"
git rm -f "$BAIT"
git commit -m "test: remove rotated credential from HEAD (history bait remains)"
git show "HEAD~1:fixtures/secrets/historical-credential.txt" > "$BAIT"
echo "Seeded trufflehog git-history bait across last 2 commits."
