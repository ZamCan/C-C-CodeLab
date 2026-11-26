#!/usr/bin/env python3
"""Compile and execute every bundled lesson example, comparing real output.

Each example ends with a comment block:

    /* expect:
    line one
    line two
    */

The block records the verified output. Examples that read standard input keep it in a sibling
``.stdin`` file. Nothing is simulated: the host compiler must compile with warnings as errors and
the produced program must print exactly the recorded output.
"""

from __future__ import annotations

import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CURRICULUM = ROOT / "app" / "src" / "main" / "assets" / "curriculum"
EXPECT = re.compile(r"/\*\s*expect:\n(.*?)\*/", re.S)


def expected(source: str) -> str:
    match = EXPECT.search(source)
    if not match:
        raise ValueError("missing /* expect: ... */ block")
    lines = [line.rstrip() for line in match.group(1).splitlines()]
    while lines and not lines[-1]:
        lines.pop()
    return "\n".join(lines)


def verify(path: Path, compiler: str, standard: str, work: Path) -> None:
    source = path.read_text(encoding="utf-8")
    want = expected(source)
    binary = work / (path.stem + ".out")
    compile_command = [compiler, standard, "-Wall", "-Wextra", "-Werror", "-O1", str(path), "-o", str(binary)]
    compiled = subprocess.run(compile_command, capture_output=True, text=True)
    if compiled.returncode != 0:
        raise RuntimeError(f"compile failed: {' '.join(compile_command)}\n{compiled.stderr}")
    stdin_path = path.with_suffix(".stdin")
    stdin = stdin_path.read_text(encoding="utf-8") if stdin_path.exists() else ""
    run = subprocess.run([str(binary)], input=stdin, capture_output=True, text=True, timeout=15, cwd=work)
    if run.returncode != 0:
        raise RuntimeError(f"exit status {run.returncode}; stderr: {run.stderr}")
    got = run.stdout.rstrip("\n")
    if got != want:
        raise RuntimeError(f"output mismatch\n--- expected ---\n{want}\n--- actual ---\n{got}")
    if run.stderr.strip():
        raise RuntimeError(f"unexpected stderr: {run.stderr}")
    print(f"ok  {path.relative_to(ROOT)}  ({len(want.splitlines())} line(s) of output)")


def main() -> int:
    if not shutil.which("gcc") or not shutil.which("g++"):
        print("gcc and g++ are required", file=sys.stderr)
        return 2
    failures = 0
    checked = 0
    with tempfile.TemporaryDirectory() as tmp:
        work = Path(tmp)
        for directory, compiler, standard in (
            (CURRICULUM / "c", "gcc", "-std=c17"),
            (CURRICULUM / "cpp", "g++", "-std=c++17"),
        ):
            for path in sorted(directory.glob("*.*")):
                if path.suffix not in {".c", ".cpp"}:
                    continue
                checked += 1
                try:
                    verify(path, compiler, standard, work)
                except Exception as error:  # noqa: BLE001 - report every failure and continue
                    failures += 1
                    print(f"FAIL {path.relative_to(ROOT)}: {error}", file=sys.stderr)
    print(f"\n{checked} example(s) compiled and executed, {failures} failure(s)")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
