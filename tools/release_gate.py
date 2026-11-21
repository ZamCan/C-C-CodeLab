#!/usr/bin/env python3
"""Fail-closed checks for a CodeLab release candidate.

This script never enables production services. It prevents a release build from
being described as production-ready while required evidence or secrets are
missing. Secrets are inspected only for presence/shape and are never printed.
"""
from __future__ import annotations

import os
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def fail(message: str) -> None:
    print(f"BLOCKED: {message}")


def main() -> int:
    blocked: list[str] = []
    wrangler = ROOT / "backend" / "wrangler.jsonc"
    if not wrangler.exists():
        blocked.append("backend/wrangler.jsonc is missing")
    else:
        config = wrangler.read_text(encoding="utf-8")
        enabled = {name: re.search(rf'"{name}"\s*:\s*"true"', config) for name in
                   ("AUTH_ENABLED", "EXAMS_ENABLED", "ISSUANCE_ENABLED")}
        if any(enabled.values()):
            blocked.append("Cloudflare feature flags must remain disabled in source configuration")

    signing = os.getenv("CODELAB_RELEASE_STORE_FILE")
    if signing:
        if not Path(signing).is_file():
            blocked.append("CODELAB_RELEASE_STORE_FILE does not point to a readable keystore")
        for key in ("CODELAB_RELEASE_STORE_PASSWORD", "CODELAB_RELEASE_KEY_ALIAS", "CODELAB_RELEASE_KEY_PASSWORD"):
            if not os.getenv(key):
                blocked.append(f"{key} is required when release signing is configured")
    else:
        blocked.append("release signing keystore is not configured")

    evidence = ROOT / "verification" / "android-device-evidence.md"
    if not evidence.exists() or "VERIFIED" not in evidence.read_text(encoding="utf-8"):
        blocked.append("physical-device evidence is missing")

    if blocked:
        for item in blocked:
            fail(item)
        return 2
    print("PASS: release prerequisites are present; run the full build and device suite before publishing")
    return 0


if __name__ == "__main__":
    sys.exit(main())
