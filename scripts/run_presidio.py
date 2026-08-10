#!/usr/bin/env python3
"""Scan fixture files for PII (Compliance Code — Microsoft Presidio)."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    try:
        from presidio_analyzer import AnalyzerEngine
    except ImportError:
        Path(args.output).write_text(
            json.dumps({"error": "presidio-analyzer not installed", "findings": []}),
            encoding="utf-8",
        )
        return 0

    analyzer = AnalyzerEngine()
    findings = []
    root = Path(args.input)
    for path in sorted(root.rglob("*")):
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8", errors="ignore")
        results = analyzer.analyze(text=text, language="en")
        for r in results:
            findings.append(
                {
                    "file": str(path.as_posix()),
                    "entity_type": r.entity_type,
                    "start": r.start,
                    "end": r.end,
                    "score": r.score,
                    "text": text[r.start : r.end],
                }
            )

    out = Path(args.output)
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(
        json.dumps({"finding_count": len(findings), "findings": findings}, indent=2),
        encoding="utf-8",
    )
    print(f"Presidio findings: {len(findings)} -> {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
