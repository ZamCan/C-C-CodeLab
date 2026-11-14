# Cloudflare examination preview

This is an **undeployed, disabled-by-default foundation**, not a live certification service. It contains a D1 migration, Worker exam routes, and pure policy tests. No hidden exam bank or issuer private key is committed or packaged in Android.

Implemented: opaque session digest lookup, per-session request limits, trusted server eligibility, 25-question two-style blueprint selection, cryptographic selection, lifetime question-ID exposure exclusion, immutable question snapshots, fixed deadlines, resume, conditional answer writes, optimistic concurrent finalization, and idempotent repeated submission. D1 batches atomically create an attempt and its questions; a partial unique index prevents multiple active attempts. SQLite constraints protect basic record integrity.

Initial machine-graded styles are multiple choice, true/false, output prediction, error identification, and algorithm selection. The schema represents all ten requested styles; unsupported styles fail closed until reviewed grading adapters exist. No promise of a permanent 1% overlap probability is made. Insufficient unseen questions returns an explicit conflict; question variants and measured overlap analytics are pending.

## Run local tests

`npm test` (Node 22+). These test policy behavior, not D1 deployment, concurrency in Workers, authentication provisioning, or signing. `python3 tools/test_backend_schema.py` from the repository root validates migration constraints and SQL state transitions against local SQLite.

## Deployment gates

1. Owner creates a preview Worker and D1 database; copy `wrangler.toml.example` to an ignored `wrangler.toml` and bind the real database ID. Apply migrations to the preview database. No deployment has been performed.
2. Configure and test a trusted identity provider and secure session provisioning/rotation/revocation. `/v1/auth/session` currently returns 503. Sessions must be random 256-bit bearer tokens; store only SHA-256 token digests. Never accept user-supplied eligibility or local Android progress as proof.
3. Populate a reviewed, independently retained server-only question bank, approved blueprint, eligibility evidence, and passing/cooldown policy. Use URL-safe question IDs. Freeze versioned course criteria. No academic passing policy is silently selected by code.
4. Implement IP/edge abuse limits in addition to authenticated-session quotas, retention cleanup for rate-limit rows, minimized audit of answers/finalization and appeals, and request-size limits at the edge. Configure a scheduled expiry sweep; active attempts otherwise expire when resumed. Provide accommodations before starting an attempt, and a reviewed outage/appeal route without silently extending deadlines. CORS is absent: a future browser client needs an explicit origin allowlist; bearer auth is not stored in cookies.
5. Exercise D1 race tests, expiry, cross-account authorization, replay, backup/restore, and live load tests before setting `EXAMS_ENABLED=true`. Authenticate deployment credentials outside Git. Unknown errors return generic responses without secret-bearing details.
6. Select an interoperable Open Badges 3.0 / VC proof profile and verify external test vectors. Implement protected Ed25519 signing, issuer key history, revocation, status privacy, PDF rendering, signed credential download, and a public verifier. These remain **unimplemented**; issuance always returns 503. Schema tables are preparation, not proof of issued credentials.
7. Implement encrypted off-provider backup and perform a real restoration drill with keys controlled separately. Document retention, deletion, key compromise, service migration, and privacy procedures before real learner data is accepted.

No Cloudflare account binding, deployment secrets, live exam, certificate PDF, signed VC, public verifier, or backup restore evidence currently exists. Do not enable the preview routes for real certificate decisions.

Implementation references: [D1 batch API](https://developers.cloudflare.com/d1/worker-api/d1-database/#batch), [Workers Web Crypto](https://developers.cloudflare.com/workers/runtime-apis/web-crypto/). Cryptography is intentionally not claimed implemented merely because the platform supports it.
