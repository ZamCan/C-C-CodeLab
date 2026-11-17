# CodeLab C & C++ — multi-agent modernization and delivery playbook
**Authority:** read with root AGENTS.md, PRODUCT_SPEC, COMPILER_ENGINE, CERTIFICATION_ENGINE, HYBRID_AUTH_STORAGE and the actual code. **Date:** 2026-11-17. **Branch:** main. This is a work order, not evidence of completed implementation.

## Mission and non-negotiable outcomes
Deliver a genuinely premium **native Android**, offline-first C17/C++17 learning academy with a real compiler, two dominant home destinations (**Compiler** and **Learn C/C++**), comprehensive structured courses, five guided projects, trustworthy persistence, optional Google/Appwrite backup, and a separate Cloudflare examination/certification system. Amazon/Cognito/DynamoDB is a disabled future adapter while AWS activation is blocked. Keep every offline feature usable without sign-in, internet, payment, or forced ads. Do not falsely claim compilation, testing, certification, or store readiness.

## Start every agent session
1. Inspect `git status`, `git log -8 --oneline`, tree, Gradle, AndroidManifest, app source, assets, backend, migrations, workflows, and latest CI results. Do not assume this document reflects the newest code.
2. Read AGENTS.md, docs/PRODUCT_SPEC.md, docs/COMPILER_ENGINE.md, docs/CERTIFICATION_ENGINE.md, docs/HYBRID_AUTH_STORAGE.md, docs/DELIVERY_PLAN.md, docs/IMPLEMENTATION_STATUS.md, backend/README.md, and aws/sync/DEPLOYMENT.md.
3. Create a concrete acceptance checklist for the slice being modified. Preserve existing code, assets, premium branding and working functionality. Prefer incremental native Java changes over a wholesale rewrite. Compose is optional only with verified migration benefits.
4. Work on the existing main branch per owner preference, in reviewable commits. Never overwrite concurrent edits; refetch before writes. Report commit SHA, changed files, exact tests run and blockers.
5. Never fabricate screenshots, real-device results, compiler stdout, release artifacts, credentials, production activation, OAuth configuration, or deployed resources.

## Engineering tracks (parallelizable by file ownership)

### A. Premium Android product and design system
**Owner files:** app/src/main/java/.../MainActivity.java, ReadingView.java, resources, icons and themes.
- Build a compact premium startup: short **CodeLab C & C++** wordmark animation, no prolonged splash; enter a native home screen with two clear dominant choices. Secondary access to progress, achievements, settings and certificate portal.
- Distinctive visual language: light-first white/neutral surfaces with refined indigo/teal accents; fully designed dark theme; subtle depth, no giant nested web-like cards or cramped text. Preserve a consistent icon set, typography scale, corner radii, elevation, touch targets >=48dp, dynamic font scaling and visible focus.
- Make screen states explicit: loading, ready, empty, offline, retry, disabled, syncing, error. No button may silently do nothing. Ensure no horizontal clipping at 320dp and large font sizes; support portrait, landscape, keyboard/IME and screen readers.
- Avoid scrolling through enormous single pages: staged lessons with Next/Back, sticky progress and resume position. C and C++ visually distinguished but use the same design system.
- Add screenshot/visual regression checks with explicit device/emulator evidence; compare to earlier user-approved logo/artifacts before changing branding.
**Done when:** clean cold boot, home navigation, theme parity, readable course and editor on real Android 8+ devices; no unresponsive controls, clipped dialogs or misleading states.

### B. Actual offline compiler and code studio
**Owner files:** OfflineCompiler, bootstrap, assets/compiler, CompilerEngine, WasiCompilerEngine, diagnostics, editor, projects.
- Inventory the bundled WASM clang/lld/sysroot/controller assets; verify license and integrity, correct MIME/asset serving, Worker/WebView messaging, runtime availability and initialization. Never claim compiler works from asset presence alone.
- Test C17 and C++17 compile/link/run on an Android device, including stdio/iostream, stdin, stderr, syntax errors, Unicode, cancellation, timeouts, repeated execution, backgrounding, low memory and renderer crash. Distinguish compilation errors from runtime errors and engine unavailability.
- Prevent arbitrary Android host process execution from untrusted source. Sandboxed WASM execution only within tested restrictions; bound source size, memory, CPU/wall time, output, file access and artifact lifetime. Reject path traversal and remote URL loading. Ensure compiler output is **real**, never static previews.
- Editor: accessible line numbers, syntax highlighting, indentation, undo/redo, font scaling, brace matching, project file tabs, explicit save and autosave, stdin panel, separate stdout/stderr, exit code and diagnostic line navigation.
- Five post-course project labs: calculator, educational login, student grades, fictional ATM and library management; real C and C++ starters, reference solutions, test cases and educational walkthroughs. Static UI preview is labeled and cannot substitute for execution.
**Done when:** repeatable device logs demonstrate compiling and executing both languages, and all project fixtures run without invented results.

### C. Curriculum, pedagogy and assessment
**Owner files:** Curriculum, Lesson, LearningPath, foundation/lessons/projects assets and verification scripts.
- Teach algorithms, pseudocode, flowcharts, decision tables, language history, compiler vs interpreter, C and C++ differences, syntax anatomy, variables/types, operators, control flow, functions, arrays, strings, pointers/references, structs/classes, file handling, debugging, safe input and capstones.
- Every symbol/keyword explanation must include name, meaning, why placed there, what changes if moved/removed, valid scope/versions, beginner mistake and an experiment. Differentiate C `int main(void)` and C++ `int main()`, `stdio.h` vs `iostream`, printf formats and iomanip.
- Include bank interest 1000 at 10% until >2000, Celsius-to-Fahrenheit, grade eligibility and traffic-light decision tables. Correct original examples and test expected outputs.
- Make progression staged and readable; add lesson review, formative feedback, offline exercises, 10 examinations × 25 items as an academic goal, with server-only final exam bank and verified eligibility. Do not bundle private final answers in APK.
- Review all factual claims and translations (English/Swahili); accessibility, learning objectives and test coverage matter more than arbitrary lesson counts.
**Done when:** lesson coverage map, human review notes, runnable examples, quizzes with answer rationale and automated validation exist.

### D. Guest + Google/Appwrite launch
**Owner files:** CloudLearningMode, LearningStore, new auth/sync adapters, manifest and Gradle.
- Guest is always default. SQLite is the offline source of truth. Google sign-in is **optional** via Appwrite Android SDK browser OAuth2 and verified `Account.get()`. Do not invent project endpoint, ID or callback scheme. The owner must configure real Appwrite project, Google OAuth provider, Android platform and redirect URL.
- Keep separate per-account local progress namespaces. Never silently merge guest data into another user's account; require explicit consent. Never leak account A progress into account B when switching, even offline.
- Appwrite row/document permissions must be enforced server-side for the authenticated owner. No client-supplied owner identity can grant access. Use bounded schema and stable course/lesson IDs.
- Sync: explicit opt-in, fetch-merge-upload, earliest completion timestamp, idempotency, bounded retries on 409, reauth on 401/403, offline queue, export and explicit cloud deletion. Local progress survives logout, failed sync and network loss; account data must not be shown to a different signed-in user.
- Never ship Appwrite API keys or Google client secrets. Client project ID and endpoint are public. Avoid a fake Google login screen; disabled state must say why.
**Done when:** real Google OAuth login, logout, account isolation, private two-device sync, offline recovery and deletion are verified against an owner-controlled Appwrite project.

### E. Cloudflare examinations and certificate trust
**Owner files:** backend/src, backend/test, D1 migrations, deployment tools, verifier dashboard.
- Preserve fail-closed flags until ready. Complete real reviewed C/C++ private question banks, versioned blueprints, safe 25-question random selection, exposure/cooldown rules, server deadline, answer persistence and idempotent scoring.
- Practical coding assessments require a separate sandboxed grader with hidden tests and quotas. Do not run untrusted native code in a Cloudflare Worker or claim practical grading works before it exists.
- Protect learner identity, passkeys/session, CSRF, authorization, verified eligibility, 70% pass threshold and three official names. Client progress is not proof of eligibility.
- Provision issuer Ed25519 private JWK only through Cloudflare secret management; publish only public key and validate matching pair. Produce downloadable signed credential and PDF/QR, independently verify signatures, support revocation and key rotation. Cloudflare is infrastructure, **not** an accrediting authority.
- D1: inspect existing schema, back up and baseline before applying migrations. Never overwrite production database or replay baseline blindly. Test recovery and privacy/deletion policies.
- Enable AUTH/EXAMS/ISSUANCE flags **sequentially only after** relevant integration, real browser/passkey and verifier tests. Never toggle to true just to meet a deadline.
**Done when:** owner-controlled live preview passes account isolation, exam/resume/deadline, signature, tamper/revocation, PDF/QR, public verifier and restore tests with evidence.

### F. Future Amazon/AWS adapter
**Owner files:** aws/, optional ProgressSync and adapter interface.
- Keep feature gated off for launch. AWS account may reject DynamoDB/Cognito with SubscriptionRequiredException; recheck before deployment, do not invent activation.
- When activated: Cognito federation with verified identity, API Gateway/Lambda, least-privilege DynamoDB, explicit provider isolation, billing alarms and 2-device tests. No AWS credentials in APK.
- Amazon users must not automatically get Appwrite data, and Google users must not automatically get DynamoDB data. Explicit verified account linking is separate future work.

### G. Ads, privacy and distribution
- No mandatory ads or paywalls for compiler/lessons. Use test ad IDs in debug, production IDs only after owner configuration and consent/legal review; handle missing network without blocking study.
- Privacy notice, data export/deletion, child/student data considerations, disclosure of online identity, no unnecessary analytics, Android target API requirements and store policy review.
- Only package release after functional gates; owner-controlled signing keystore and version bump, checksums, provenance and rollback. Amazon Appstore is an optional channel, not proof of Google Play approval.

## Verification commands (run, do not merely quote)
```sh
python3 tools/verify_lessons.py
python3 tools/verify_projects.py
python3 tools/test_backend_schema.py
npm ci --prefix backend
npm test --prefix backend
npm run build --prefix backend
npm run check --prefix backend
npm test --prefix aws/sync
bash gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug
```
Android build is manual-only in GitHub Actions; never assume push runs an APK build. Validate commands against actual package scripts first; if unavailable, record as NOT RUN, not PASS. Use Android SDK and JDK17; inspect Gradle configuration before changing dependencies. Physical device tests remain mandatory for compiler and OAuth.

## Release decision gates and truthful status
**Gate 0 — source consistency:** no uncommitted accidental changes; tests pass; dependency and secret scan clean.
**Gate 1 — learning:** home, editor, SQLite, curriculum, projects and compiler are functional offline on devices.
**Gate 2 — Google:** configured Appwrite project, real browser OAuth, private sync and isolation verified; otherwise ship guest-only only with owner approval, not pretend Google works.
**Gate 3 — certification:** Cloudflare signing, exams, verifier and D1 validated; otherwise keep issuance disabled and clearly label certification unavailable.
**Gate 4 — distribution:** app signing, policy review, release notes, versioning, install/update/rollback tested and owner approves publishing.
Do not conflate a build artifact with a production release. Report each gate as PASS / FAIL / BLOCKED with evidence URL, commit, test command and device model.

## Agent handoff format
At end of every meaningful slice record:
- Scope, files modified, commit SHA and branch.
- Tests run with actual results; tests not run and reason.
- Before/after behavior and screenshot/log evidence where applicable.
- Security/data migration effects, secrets needed from owner, external blockers.
- Next highest-impact implementable task. Continue coding if work remains; do not stop at a cosmetic change and a status message.

## Immediate execution queue (highest impact first)
1. Audit compiler boot and prove real C and C++ execution on physical Android.
2. Establish owner Appwrite endpoint/project ID; implement actual Android SDK OAuth, per-user SQLite namespace and secure sync.
3. Fix backend test regressions; run all certification tests; prepare reviewed exam content without synthetic answers in production.
4. Verify Cloudflare D1 migration baseline, issuer key provisioning and independent certificate verification; leave flags disabled until safe.
5. Polish native home, editor and lesson flow with screenshots and accessibility checks.
6. Run full quality gates; only then produce an approved signed release.
