# Certification and compiler verification — 2026-11-16

## Verified through connected services
- GitHub main contains backend/src/worker.mjs, backend/src/policy.mjs, backend/wrangler.jsonc, backend/migrations/0001_initial.sql, and Android CertificationClient.java.
- Cloudflare ZamCan Nexus account 4603d64520de2639996e13d44985f4fa has D1 zamcan-codelab-certification (c3efb3e2-9e4d-42a4-89f8-9a54016c2978). Initial schema was previously applied.
- 2026-11-16: Uploaded modular Worker zamcan-codelab-certification to Cloudflare with D1 binding DB and plain-text EXAMS_ENABLED=false. Cloudflare returned success, deployment ID 1819111f95734bc798e7655e80097c76. Subsequent Worker listing confirms its existence.
- This is deployment existence verification, **not** HTTP endpoint reachability, identity, exam, grading, signed certificate or production readiness verification.

## Verified source limitations — block live activation
1. Android offline compiler is still scaffolded: inspect WasiCompilerEngine and OfflineCompilerBootstrap; do not claim real Android C17/C++17 compilation until native/WASI toolchain, sysroot, runner and on-device test artifacts exist.
2. Current backend/src/policy.mjs supports only five single-choice automatic styles and mandates exactly two styles per 25-question attempt. Ten-family exam specification (including multi-part and practical coding) is not implemented. Current scoring uses 4 points per question and index-only answers.
3. Worker /v1/auth/session is explicitly unavailable (no verified identity provider), /v1/credentials/issue is explicitly unavailable, and there is no implemented /v1/credentials/:id/status verifier despite the Android client calling that route.
4. No verified question bank, authenticated learner enrollment, practical code isolation, trusted mastery eligibility, issuer signing key lifecycle, credential revocation, or end-to-end exam test evidence.
5. Worker listing does not establish a reachable workers.dev hostname or custom route. Verify URL/health, database binding, denied protected endpoints, and deployed script configuration separately.
6. Before live exams, inspect concurrency, expired active attempts, cooldown policy, rate-limit cleanup, error handling, D1 migration state, session security, and full threat model. Never put grading keys in Android or public API responses.

## Next implementation gates
A. Get a real offline compiler runtime working on Android, with C17/C++17 integration tests and captured compiler stderr/stdout/exit code.
B. Verify deployed Worker HTTP health, D1 bindings, identity, audited bank and server-authoritative timed exams. Expand ten family graders and isolated coding evaluator before activating exams.
C. Implement verifiable signed credentials, public verification, revocation, issuance policy and negative security tests; keep issuance disabled until all pass.
D. Only then integrate AdMob using consent/privacy requirements, no ads in exams or code editor, and no interference with offline learning.

**Current release classification:** infrastructure deployed, exam/certification and offline compiler not production ready.
