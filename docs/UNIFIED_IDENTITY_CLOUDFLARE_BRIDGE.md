# CodeLab unified identity — Google/Appwrite → Cloudflare
**Binding decision, 2026-11-17.** A Google sign-in is an **application-wide CodeLab sign-in**, not an Appwrite-only feature. This document is an implementation contract; the trust bridge is NOT yet implemented or tested.

## User experience
- Guest: offline compiler, lessons, quizzes, projects and SQLite without an account. Optional online certification requires a verified account.
- Google: one sign-in through the Appwrite Android SDK. After verified login, the same CodeLab account is used for optional cloud progress, authenticated exam registration, eligibility checks, exam attempts, certificate ownership, revocation/status and private certificate downloads. Public credential verification must work without login.
- Cloudflare can show an unavailable/reauthenticate state but must not silently ask the learner to register an unrelated account after successful CodeLab Google sign-in.
- Amazon (future): separate Cognito-verified adapter that maps into the same canonical learner model, but do not enable or auto-link identities until tested.

## Critical trust boundary
**Do not** forward Appwrite session cookies to Cloudflare or accept `email`, `google_id`, `appwrite_user_id`, `learner_id`, or `is_authenticated` as proof from Android JSON. A bearer token cannot be trusted simply because the app says it came from Appwrite. Do not put a server API key or private JWT-signing key in APK.

Recommended first implementation: Cloudflare Worker validates an Appwrite-issued short-lived JWT server-to-server using Appwrite's authenticated account endpoint (or rigorously validates a supported signature with pinned issuer/audience/JWKS, expiry and token type). Verify exact Appwrite project `codelab-zamcan` and endpoint `https://fra.cloud.appwrite.io/v1`; reject wrong issuer/project, expired/revoked sessions, invalid signatures, absent verified user, replayed or cross-account assertions. Prefer live account verification over locally trusting unverified JWT claims. Document precise Appwrite API contract and revocation semantics before implementation.

### Mobile exchange protocol (new endpoint, not yet built)
1. Android performs Appwrite Google OAuth2 using browser/CallbackActivity, then obtains `Account.get()` and a **short-lived Appwrite JWT** through the authenticated Appwrite SDK; never use a permanent API key.
2. Android calls Cloudflare `POST /v1/mobile/appwrite/exchange` with Appwrite JWT, an installation-scoped PKCE code challenge or nonce, and app version. Enforce TLS, body bounds, rate limits and no token logging.
3. Worker validates token **against Appwrite**, verifies project and account, resolves an immutable canonical CodeLab learner ID by unique key `appwrite:<verified-user-id>`, and atomically creates it if absent. Never identify by mutable email or display name. Bind canonical learner to that verified external identity with a unique database constraint.
4. Worker issues a short-lived **Cloudflare-owned** bearer session with a separate audience and bounded expiry; Android stores it securely, never as plaintext in a backup, and refreshes via a fresh Appwrite-authenticated exchange. Logout clears both client sessions; Cloudflare session revocation should be supported.
5. Existing Cloudflare exam/credential endpoints use only verified Cloudflare sessions and server-side learner IDs; the browser passkey login may remain as a separate account path, but account linking must require both independently verified identities and explicit consent. Never automatically merge accounts by email.
6. Appwrite TablesDB retains **progress sync only**. Cloudflare D1 remains authoritative for examination eligibility, attempts, grades, certificate signatures, revocation and public verification. Local/Appwrite lesson completions alone cannot grant certification eligibility.
7. No secret, Appwrite JWT, Cloudflare token or user PII may appear in URL query strings, analytics, logs, crash reports, screenshots or Git.

## Cloudflare schema and backend migration tasks
- Inspect existing D1 migrations, `learners`, `sessions`, `auth_challenges`, `mobile_codes`, and `backend/src/auth.mjs` before coding.
- Add `external_identities` with `provider`, `provider_subject`, `learner_id`, `created_at`, unique(provider,provider_subject), foreign key learner. Alternatively reuse a verified unique `account_ref` if it safely supports this; do not introduce duplicate identities.
- Implement token verification as a dedicated adapter, injected and mocked in tests; reject unauthenticated calls, wrong project, invalid/revoked JWT, different user, duplicate mapping races and session replay. Add timeouts and safe error responses for Appwrite downtime.
- Preserve current passkey endpoints and `/v1/mobile/exchange` PKCE flow. Do not bypass existing AUTH_ENABLED gate; leave all issuance/exam flags disabled until proven safe.
- Never trust `identity_verified` based solely on Google login; legal name and certificate identity verification are separate, with explicit review and audit trail.
- Ensure Google-signed-in users can view their own exam attempts and certificates, but not other learners' records.

## Test matrix
1. Google account A authenticates, gets canonical learner A, signs out and cannot reuse revoked Cloudflare session.
2. Google account B on same phone gets learner B and cannot access A's progress, exams or certificates.
3. Tampered/expired/revoked/wrong-project JWT rejected; raw email or user ID rejected.
4. Repeat exchange idempotently resolves the same learner; concurrent first login creates only one identity.
5. Appwrite outage does not break guest offline learning; online certification fails closed.
6. Exam attempts remain server-authoritative; forged progress or pass claims rejected.
7. Passkey learner never silently merges with Google learner of same email.
8. Two-device Google login maps to same learner and only own records.
9. Public certificate verification is accessible anonymously and verifies signature/status independently.
10. Full real-device OAuth callback, token refresh, offline/online transitions and process restart are exercised.

## Agent delivery and acceptance
Implement the above as tested vertical slices. Record actual code changes, migrations, tests, and CI runs in `docs/DELIVERY_PLAN.md`. Do not enable Google sign-in UI or certificate issuance until their relevant security and device gates pass. Current Appwrite project/database provisioning does **not** imply this bridge exists.
