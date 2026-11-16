"""Read-only compatibility check for a D1 schema exported without student data."""
import json
from pathlib import Path
import re
import sqlite3
import sys
root = Path(__file__).resolve().parents[1]
source = Path(sys.argv[1]) if len(sys.argv) > 1 else root / 'verification/cloudflare-existing-schema.json'
response = json.loads(source.read_text())
rows = response[0]['results']
remote = {row['name']: row for row in rows}
local = sqlite3.connect(':memory:')
local.executescript((root / 'backend/migrations/0001_initial.sql').read_text())
expected = {name: {'type': kind, 'sql': sql} for name, kind, sql in local.execute("SELECT name,type,sql FROM sqlite_master WHERE name NOT LIKE 'sqlite_%'")}
def normalize(sql):
    return re.sub(r'\s+', ' ', sql.strip().rstrip(';')).lower()
problems = []
for name, row in expected.items():
    actual = remote.get(name)
    if not actual or actual['type'] != row['type'] or normalize(actual['sql']) != normalize(row['sql']):
        problems.append(name)
extras = sorted(set(remote) - set(expected))
if problems or extras:
    print('Manual migration review required. Different/missing:', problems, 'Additional:', extras)
    sys.exit(1)
print('Existing remote schema matches migration 0001 exactly after whitespace normalization.')
print('Do NOT replay 0001. Back up and baseline migration history before applying 0002 and 0003.')
print('Read-only schema inspection: no learner records or remote database writes.')
