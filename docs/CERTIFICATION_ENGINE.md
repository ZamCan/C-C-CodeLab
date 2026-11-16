# ZamCan Academy — Cloudflare online examination and verifiable certificate architecture

## Decisions
All lessons, flashcards, exercises, practice quizzes, projects and local compilation are free/offline. Final certificate exams require connectivity. Hybrid storage: learner owns downloadable PDF and portable signed credential; server keeps minimal issuance, status, audit and identity references. Launch target zero mandatory platform fees, **subject to free-tier quotas and operational feasibility**. Cloudflare is hosting infrastructure, not a certificate accreditor. 1EdTech Open Badges 3.0 and W3C VC are standards, not endorsement or free hosted certificate accounts.

## Exam policy
- Online-only, 25 questions per attempt, ten question-design styles: MCQ, true/false, output prediction, error identification, code completion, code ordering, debugging, trace tables, algorithm selection, practical coding.
- The user proposed selecting two of ten styles per attempt. Implement a configurable blueprint policy that balances topic/difficulty coverage. Confirm exact distribution during pedagogical review; do not force only two styles if it makes competency measurement invalid.
- Large versioned question bank, difficulty tags, learning outcomes, random parameter generation, per-user exposure history. Cryptographically secure server-side selection, no leaked answer keys or seeds.
- Requested repeat probability **0.01**: define and measure precisely (per-question vs any-overlap). With finite banks and unlimited retakes, permanent 1% overlap cannot be guaranteed. Implement no-repeat windows, question variants, overlap metrics, sufficient-bank thresholds and controlled fallback/eligibility policy.
- Server-issued attempt ID, randomized blueprint, fixed start/deadline, secure timer, answer submission and idempotent finalization. Client timer is informational only.
- Force-close cannot be prohibited by Android; app switching cannot be reliably prevented. Exit/background does not stop server deadline. Reopening resumes existing attempt without extra time; expiry is final per policy. Add grace for network delivery and an accessible review/appeal path for legitimate outages. Rate-limit abandoning attempts and enforce documented cooldown.
- User-approved final exam pass mark: 70% or above (2026-11-16), enforced server-side. Record three official names before the exam, match a verified identity, snapshot the name, and require explicit reconfirmation after passing before signing a certificate. Completed prerequisites and genuine project competence remain eligibility requirements.
- For coding exams, compile and execute submissions only in a dedicated isolated grading service with hidden tests and quotas. Cloudflare Workers orchestrate; do not execute arbitrary native binaries inside a Worker. For zero-cost MVP, choose securely gradable question types until isolated grading is funded/tested.

## Trust boundaries
`Android offline content` → `authenticated Cloudflare Worker API` → `D1 records` → `eligibility decision` → `protected issuer signing` → `downloadable credential` → `public verifier`.
Offline local progress is user-modifiable and cannot alone authorize issuance. Require server-verifiable completion/assessment evidence. Identity verification should be proportionate and privacy-preserving.

## Suggested D1 schema (illustrative, subject to migrations)
- learners(id, account_ref, created_at)
- courses(id, version, achievement_criteria_hash)
- exam_blueprints(id, course_id, policy_version, active)
- questions(id, course_id, style, objective, difficulty, version, encrypted_content_ref)
- attempts(id, learner_id, course_id, blueprint_id, started_at, deadline_at, status, score, finalized_at)
- attempt_questions(attempt_id, question_id, variant_seed, position, answer_commitment)
- answer_submissions(attempt_id, question_id, response_ref, submitted_at)
- credentials(id, learner_id, course_id, course_version, issued_at, signing_key_id, credential_digest, status, revoked_at)
- audit_events(id, actor_ref, event_type, object_ref, created_at, metadata_minimized)
Keep personal information minimal; design deletion, retention, backup and subject access policies.

## API contract draft
- `POST /v1/auth/session` authenticate and establish secure learner session
- `GET /v1/courses/:id/eligibility` display trusted eligibility status
- `POST /v1/exams/attempts` create/reuse authorized active attempt
- `GET /v1/exams/attempts/:id` resume with remaining server time
- `PUT /v1/exams/attempts/:id/answers/:questionId` idempotent save
- `POST /v1/exams/attempts/:id/submit` finalize once, reject after deadline
- `POST /v1/credentials/issue` authorized idempotent issuance
- `GET /v1/credentials/:id/status` minimal public status, rate limited
- `GET /.well-known/zamcan-issuer-keys.json` issuer public key set and rotation metadata
- `GET /verify/:id` privacy-safe verification interface
Specify auth, error types, schema validation, CORS, CSRF considerations, rate limits, audit and replay defenses before implementation.

## Credential data and cryptography
Use a standards-conformant Open Badges 3.0 / W3C VC credential profile; choose and document a valid Ed25519-compatible proof suite and canonicalization process, and use interoperable test vectors. Ed25519 is a **signature**, not encryption. Keep private key in protected server secret/key service, never APK, repository or public page. Include issuer, subject identity strategy, achievement/course version, criteria, issue date, unique ID, proof/key ID and optional expiration. Generate polished PDF as human-readable rendering of the signed credential, embed QR to verifier and download signed machine-readable credential. Digital signature and decorative academy seal must not be confused with accreditation.

Verifier checks trusted issuer key, proof, payload integrity, dates, course metadata and current revocation/status; distinguish **valid signature**, **valid current status**, **unknown**, **revoked**, and **offline unable to check status**. Avoid leaking learner private details via predictable URLs. Public key rotation must retain historic keys; revoke compromised keys as policy requires.

## Cloudflare deployment
- Pages: static verifier and privacy policy.
- Workers: exam API, issuance and status endpoints.
- D1: minimal records; use migrations and parameterized queries.
- Secrets: deployment-managed issuer key, authentication secret and API credentials; never commit.
- Protect endpoints using authentication, least privilege, rate limits, input validation, audit and abuse monitoring.
- Use free tier initially where allowed. Track actual CPU/request/storage/build limits and costs; do not equate 100k Worker requests/day with 100k free issued certificates.
- Choose pages.dev/ workers.dev hostnames until optional domain purchase. Backups must be independent of provider.

## Recovery and lifecycle
Encrypted off-provider backups of minimal database and signing key recovery material, separately controlled and access logged. Verify restore. Store signing public key history, revocation status, credential schemas and course version mappings. Have compromised-key, duplicate issuance, revocation and service migration procedures. Minimize retained PII and make a privacy notice appropriate to Tanzania and target markets.

## Acceptance tests
Forged credential, altered recipient/course/score, stale/revoked status, unknown key, key rotation, duplicate request, replay, expired attempt, force-close/resume, offline/unstable connectivity, simultaneous submits, high request volume, backup restore, privacy enumeration. No 'certified' claim before real tests and a live public verifier.
