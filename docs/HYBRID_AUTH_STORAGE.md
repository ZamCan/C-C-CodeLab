# CodeLab C & C++ — approved hybrid learning architecture

## Launch modes

1. **Guest** (default): no account, all local learning data in Android SQLite, offline lessons/compiler. Guest progress is never uploaded automatically.
2. **Google via Appwrite** (launch target): explicit opt-in, Appwrite Android SDK OAuth2 Google session and Appwrite Databases for private per-user progress; local SQLite remains the offline source of truth. Sync is resumable and merge-only for completed lessons. The app must not demand sign-in to learn.
3. **Amazon via AWS** (feature-gated future): Amazon identity verified through Cognito, DynamoDB via authenticated API Gateway/Lambda; never ship AWS keys. Hidden until AWS services are activated and integration-tested.
4. **Cloudflare**: separate, server-authoritative examination eligibility, secure examination attempts, Ed25519 credential issuance, public verification. Cloudflare must not accept Appwrite/AWS progress as proof of exam eligibility.

## Separation and identity boundaries

Do not share provider tokens or database records across backends. One account can only read its own Appwrite rows using server-enforced row permissions, not merely client-side filters. Do not derive learner identity from an unverified email. Use `appwrite_user_id` as the Appwrite row owner; AWS uses Cognito `sub`. A future cross-provider link requires both independently verified sessions and explicit consent. Cloudflare maintains its own verified certificate identity.

## Appwrite configuration needed before live Google sign-in

- Create a project in the owner's Appwrite account; record **public** HTTPS endpoint, project ID and Android application ID. Never commit API keys or Google OAuth client secrets.
- Register Android platform `com.zamcan.codelab` (confirm exact Gradle applicationId).
- Enable Google OAuth provider in Appwrite and configure the exact Google callback URL shown by Appwrite in Google Cloud. Register the Appwrite Android SDK `io.appwrite.views.CallbackActivity` with `appwrite-callback-<PROJECT_ID>` scheme; project ID must be the real ID, never a placeholder.
- Use Appwrite Android SDK `Account.createOAuth2Session(activity, OAuthProvider.GOOGLE)`, then verify `Account.get()` before enabling sync. Persist sessions only through the SDK; logout revokes Appwrite session. Use browser-based OAuth, not a custom WebView.
- Create a database/table (or collection, depending on server version) for progress, with row/document-level permissions enabled. Each progress record is private to `user:<authenticated user id>`; create/read/update/delete must be restricted to the owner. No public/list-anyone permission.
- Keep schema `schema=1`, `course`, `lesson`, `completed_at` (positive Unix milliseconds), and optional server revision. Prefer one deterministic document ID per course/lesson per account, and reject duplicate identifiers. Enforce bounded values, server-side ownership and size limits.
- Implement and test offline-to-online migration: only after explicit confirmation, upload union of local completions; earliest valid completion timestamp wins. On 401/403 stop and require reauthentication. On 409 refresh and retry boundedly. Never delete local progress on network error or sign-out.
- Test two Google accounts on one device: no account A data must be displayed or uploaded under account B. Keep guest data separate from each signed-in account; require explicit merge when switching. A single shared SQLite progress table is **not** sufficient for this privacy boundary.
- Offer export, explicit cloud deletion, sign-out and a privacy notice before public release.

## Useful optional Appwrite features

- Auth and Databases: core launch needs.
- Functions: optional server validation and abuse controls, not required for basic private progress.
- Realtime: optional signed-in progress refresh; not necessary for offline-first learning.
- Storage: optional encrypted learner-owned backup or project assets, not source code by default.
- Messaging: optional reminders only with opt-in and platform permission; local Android notifications work offline.
- Do not add a feature merely because a free quota exists; check current Appwrite plan, region, rate limits and billing first.

## Release gates

1. Actual Appwrite endpoint/project and Google provider callback configured.
2. Android SDK dependency and OAuth callback wired, sign-in/out and private per-user sync implemented.
3. Account isolation, conflict handling, offline recovery, deletion and consent tested on devices.
4. Cloudflare remains disabled for issuance until independent exam/identity/key/review gates pass.
5. AWS Amazon path stays hidden while subscription remains blocked.
6. Compiler and learning content must pass physical-device testing before Android release.

**Status:** This is the implementation contract, not evidence that Appwrite is provisioned or the Android OAuth path is complete.
