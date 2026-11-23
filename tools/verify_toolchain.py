"""Verify offline assets against reviewed hashes, including worker-side pins."""
from pathlib import Path
import argparse
import zipfile
import gzip
import hashlib
import json

parser = argparse.ArgumentParser()
parser.add_argument("--apk", type=Path)
args = parser.parse_args()

root = Path(__file__).resolve().parents[1]
assets = root / 'app/src/main/assets/compiler'
manifest = json.loads((root / 'docs/compiler-artifacts.json').read_text())
worker = (assets / 'worker.js').read_text()
for name, expected in manifest['artifacts'].items():
    data = gzip.decompress((assets / (name + '.gz')).read_bytes())
    if args.apk:
        with zipfile.ZipFile(args.apk) as apk:
            assert apk.read('assets/compiler/' + name) == data, name + ': invalid APK payload'
    assert len(data) == expected['bytes'], name + ': wrong size'
    assert hashlib.sha256(data).hexdigest() == expected['sha256'], name + ': wrong digest'
    assert expected['sha256'] in worker, name + ': missing runtime pin'
    if name != 'sysroot22.tar':
        assert data[:8] == b'\0asm\x01\0\0\0', name + ': invalid WASM header'
    print('PASS:', name, expected['sha256'])
for notice in ['LICENSE', 'LICENSE.llvm', 'NOTICE', 'wasi-libc-LICENSE', 'musl-COPYRIGHT']:
    assert (assets / 'licenses' / notice).stat().st_size > 0, notice
print('PASS: compiler artifact integrity and notices')
