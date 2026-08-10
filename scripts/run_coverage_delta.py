#!/usr/bin/env python3
"""Emit coverage-delta artifacts for taxonomy Coverage Delta runner."""

from __future__ import annotations

import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def counter(root: ET.Element, metric: str) -> tuple[int, int]:
    covered = missed = 0
    for c in root.iter("counter"):
        if c.get("type") == metric:
            covered += int(c.get("covered", 0))
            missed += int(c.get("missed", 0))
    return covered, missed


def pct(covered: int, missed: int) -> float:
    total = covered + missed
    return round(100.0 * covered / total, 2) if total else 0.0


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    current = root / "target" / "site" / "jacoco" / "jacoco.xml"
    baseline_candidates = (
        root / "target" / "jacoco-baseline.xml",
        root / "baselines" / "jacoco-baseline.xml",
        root / ".testable" / "jacoco" / "jacoco-baseline.xml",
    )
    baseline = next((p for p in baseline_candidates if p.is_file()), None)
    out_dir = root / "reports" / "coverage-delta"
    out_dir.mkdir(parents=True, exist_ok=True)
    # .testable/ is NOT gitignored — commit the computed delta here so a worker that
    # only reads static evidence (no live `mvn test` run) still finds a real result.
    testable_dir = root / ".testable" / "coverage-delta"
    testable_dir.mkdir(parents=True, exist_ok=True)

    if not current.is_file():
        print(f"Missing current JaCoCo report: {current}", file=sys.stderr)
        return 1
    if not baseline or not baseline.is_file():
        print(
            "Missing baseline JaCoCo report (tried target/jacoco-baseline.xml, "
            "baselines/jacoco-baseline.xml, .testable/jacoco/jacoco-baseline.xml)",
            file=sys.stderr,
        )
        return 1

    cur_tree = ET.parse(current)
    base_tree = ET.parse(baseline)
    cur_root = cur_tree.getroot()
    base_root = base_tree.getroot()

    metrics = {}
    for name in ("INSTRUCTION", "LINE", "BRANCH", "METHOD"):
        c_cov, c_miss = counter(cur_root, name)
        b_cov, b_miss = counter(base_root, name)
        cur_pct = pct(c_cov, c_miss)
        base_pct = pct(b_cov, b_miss)
        metrics[name] = {
            "current_percent": cur_pct,
            "baseline_percent": base_pct,
            "delta_percent": round(cur_pct - base_pct, 2),
        }

    summary = {
        "current_report": str(current.as_posix()),
        "baseline_report": str(baseline.as_posix()),
        "line_delta_percent": metrics["LINE"]["delta_percent"],
        "branch_delta_percent": metrics["BRANCH"]["delta_percent"],
        "metrics": metrics,
    }
    (out_dir / "coverage-delta.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
    (testable_dir / "coverage-delta.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
    print(json.dumps(summary, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
