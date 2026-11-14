#!/usr/bin/env python3
"""Compile every worked lesson using host compilers; this is NOT Android execution proof."""
import os
from pathlib import Path
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'app/src/main/java/com/zamcan/codelab'

def run(args, **kwargs):
    return subprocess.run([str(arg) for arg in args], check=True, timeout=45, **kwargs)

with tempfile.TemporaryDirectory(prefix='codelab-lessons-') as temp:
    work = Path(temp)
    javac = os.environ.get('JAVAC', 'javac')
    java = os.environ.get('JAVA', 'java')
    run([javac, '-d', work, JAVA / 'Lesson.java', JAVA / 'Curriculum.java', ROOT / 'tools/ExportLessons.java'])
    run([java, '-cp', work, 'ExportLessons', work])
    sources = sorted(list(work.glob('*.c')) + list(work.glob('*.cpp')))
    assert len(sources) == 20
    for source in sources:
        cpp = source.suffix == '.cpp'
        executable = work / source.stem
        run(['c++' if cpp else 'cc', '-std=c++17' if cpp else '-std=c11', '-Wall', '-Wextra', '-Werror', source, '-o', executable])
        run([executable], input='5\n', text=True, capture_output=True, cwd=work)
    print('PASS: all 20 worked examples compile with warnings treated as errors and exit successfully on the host.')
