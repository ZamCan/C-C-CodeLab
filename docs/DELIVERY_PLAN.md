# Engineering delivery ledger — ZamCan Academy

**Target:** Monday 2026-11-16 for a real installable preview if achievable. **Reality rule:** dates are goals, not evidence. Keep this ledger updated with commit, command, output and device test evidence.

## Observed starting state (2026-11-14)
- Repository README states offline compiler not integrated and Android APK/on-device native compilation not verified.
- docs/ARCHITECTURE.md, docs/EXAM_POLICY.md, docs/PROJECTS.md, host C/C++ smoke-test workflow, hello examples and library_management.cpp were committed in earlier work.
- This documentation batch establishes master agent instructions and implementation specifications; it does **not** prove an APK, live compiler, Cloudflare deployment, or completed lessons.

## Milestone A — inspect and preserve existing app
- [ ] Inventory source tree, branches, assets, logos, Gradle configs, CI, previous builds.
- [ ] Select and document existing Android architecture; preserve prior premium work.
- [ ] Establish baseline reproducible Android build with exact logs.

## Milestone B — core offline app
- [ ] Premium wordmark boot and two-home navigation.
- [ ] Project workspace, editor, C/C++ highlighting, four-space indentation and local persistence.
- [ ] **Real Android offline C compiler** compile/run on device, diagnostics and stdin.
- [ ] **Real Android offline C++ compiler** compile/run on device, diagnostics and stdin.
- [ ] Safety limits, cancellation, isolation assessment, multiple files and export.
- [ ] Test on modest physical Android hardware in airplane mode.

## Milestone C — academy content
- [ ] Subject-neutral lesson/progress/assessment models.
- [ ] C curriculum with deep token-by-token lessons and exercises.
- [ ] C++ curriculum with deep token-by-token lessons and exercises.
- [ ] Offline quizzes, flash questions, corrections, mastery tracking and accessible UI.
- [ ] Five guided projects with real source, local tests and separate static previews.

## Milestone D — online certificate path
- [ ] Cloudflare account/project binding and environment secrets (user action may be needed).
- [ ] D1 schema and migrations, authenticated Worker APIs.
- [ ] Server randomized 25-question attempts and ten question styles.
- [ ] Timer, resume, expiry, cooldown, replay and overlap policies.
- [ ] Eligibility authorization, signed Open Badge/VC, learner PDF/JSON download.
- [ ] Public verifier, key rotation, status/revocation and privacy.
- [ ] Independent encrypted backups and tested restore.

## Milestone E — quality and delivery
- [ ] Unit tests, integration tests, Android instrumented tests and CI green.
- [ ] Actual APK built; artifact link, SHA-256, installation and launch evidence.
- [ ] Test full offline course → projects → online exam → certificate → public verification.
- [ ] Accessibility, localization scaffolding, performance, security and legal review.
- [ ] Release notes clearly identify unfinished functionality.
- [ ] Production readiness only after every security/functional gate passes.

## Prioritization
A working honest vertical slice is better than a polished nonfunctional facade. For Monday preview, prioritize A + B and a representative lesson/project; do not claim full production certification is ready unless D and E are truly tested.

## Agent handoff template
Date; agent; branch/commit; changed paths; commands run; pass/fail logs; APK checksum and tested device if any; known blockers; next exact tasks.


## Implementation ledger — 2026-11-14 preview slice

Specification baseline: `ba198369a745100020e60422229c91a00100f7c6`. Implementation changes are on branch `preview/android-foundation`; the implementation commit is recorded in the following handoff entry. Existing specification files, C/C++ samples, host smoke tests, and Git history were preserved. The earlier locally edited README was retained in a named Git stash before fast-forwarding to the new specifications.

| Area | Status | Evidence / limitation |
| --- | --- | --- |
| Repository inspection and instruction sync | done | Read AGENTS.md and every engineering specification; initial repository had no Android application |
| Native Android build | done | `verification/android-build.log`; actual debug APK created |
| Identity and home | partial | ZamCan Academy identity, exactly two primary destination panels, secondary achievements/status; polished animated wordmark and final visual review pending |
| Glass-style UI | partial | Native translucent gradient panels, aurora background, rounded ripple controls; no real backdrop blur, light theme pending, no visual-device sign-off |
| Editor/project persistence | partial | Isolated project folders, multiple source files, UTF-8 atomic save, lexical highlighting, line gutter, four-space Enter/Tab indentation, export and font size; rename/delete/import, undo/redo, bracket matching, wrap modes, diagnostic navigation and background I/O remain |
| Learning data | partial | Subject-neutral SQLite progress keys and one-time legacy migration; backup/import UI and comprehensive migration/device tests remain |
| Lessons | partial | 20 introductory lessons, 20 host-tested worked examples, practice feedback, detailed first-program token anatomy and math notes; substantial staged curriculum, spaced repetition, bookmarks, varied quizzes and editorial review remain |
| Five projects | partial | Five offline guides, ten procedural C/C++ reference builds, ten starters, sample transcripts, milestones, algorithms and debugging exercises; references are educational and bounded, not hardened production systems; idiomatic C++ expansion and Android execution remain |
| Native offline compiler/runtime | blocked | No integrated licensed Android toolchain; no device execution/isolation proof. See COMPILER_RESEARCH.md. No simulated output |
| Cloudflare exams | partial | D1 migration, disabled Worker API, authenticated session lookup, eligibility, 25-question/two-style policy, freshness exclusion, fixed deadline/resume, guarded writes/finalization; undeployed, no identity provider or reviewed exam bank, only five automatically gradable styles |
| Credential path | not started | Schema and fail-closed endpoint only; no signed interoperable VC, PDF, protected key deployment, live verifier, revocation flow or encrypted restoration proof |
| Localization/accessibility | partial | English/Swahili navigation/status resources, RTL scaffolding, native controls and scalable text; teaching content remains English, light theme/TalkBack/large-font verification pending |
| Physical device verification | blocked | User confirmed installation target is this same phone; ADB returned no devices. Opening installer is separate from proven installation, launch and functional testing |

### Completed verification

- Android: `tools/verify_android.sh --offline` using Gradle 8.9, Android SDK 35, Java 21, and the installed Termux AAPT2 override. Build log retained in `verification/android-build.log`. Gradle tasks: `testDebugUnitTest lintDebug assembleDebug`.
- Unit tests: 12 tests, zero failures/errors. Suites: com.zamcan.codelab.WorkspaceRepositoryTest, com.zamcan.codelab.EditorRulesTest, com.zamcan.codelab.CurriculumTest, com.zamcan.codelab.ProjectStoreTest. XML/HTML reports under `app/build/test-results/` and `app/build/reports/` (generated, not committed).
- `python3 tools/verify_lessons.py`: all 20 original worked examples compiled with C11/C++17, `-Wall -Wextra -Werror`, and exited successfully on the host.
- `python3 tools/verify_projects.py`: ten reference builds and exact sample transcripts passed; calculator failure statuses/stderr and corrupt record-load rejection passed.
- `node --test backend/test/policy.test.mjs`: eight policy/disabled-endpoint tests passed locally.
- `python3 tools/test_backend_schema.py`: six SQLite schema/state tests passed, including active-attempt uniqueness, stale-submit rejection, expiry, finalized-write rejection, rate-limit updates and an in-memory backup round-trip. This is **not** an encrypted off-provider disaster-recovery drill or live D1 proof.
- `adb devices`: empty device list. No screenshots, physical-device automation, airplane-mode runtime proof, or end-to-end certificate journey available.
- Initial build caught a lesson constructor error; subsequent lint caught an API-27-only theme attribute used with minSdk 26. Both were corrected before the successful build; no lint baseline suppression was added.

### Preview artifact

- APK: `app/build/outputs/apk/debug/app-debug.apk`
- SHA-256: `e60bd3153ab8220dfcab249745f2bfce518356483a98d948a11994cd83a6d8e0`
- Package: `com.zamcan.codelab`; version `0.1.0`; development/debug signing only.
- APK size: 75800 bytes.
- Checksum file: `verification/apk-sha256.txt`.
- Remote CI workflow exists, but remote CI success is not claimed.

### Next exact tasks toward Monday 2026-11-16

1. Install this APK on the same phone, confirm successful launch, inspect all screens, test save/restart/rotation/export and large text, and capture model/OS/ABI and screenshots through an authorized ADB connection.
2. Execute the bounded native toolchain investigation in COMPILER_RESEARCH.md. Verify real C and C++ execution and safety on the phone before exposing Compile/Run.
3. Harden the portable project parsers, add idiomatic C++ alternatives, expand the first deep lesson pattern through both courses, and complete light-theme/localization/editor requirements.
4. Obtain owner-controlled Cloudflare bindings and reviewed identity/exam policy; deploy only to a preview environment after integration/race tests. Finish interoperable credential proofs and verifier tests before accepting real certificate requests.
5. Record device evidence and reassess the preview acceptance criteria. The Monday date remains a target, not evidence of completion. No production-readiness claim is made.

### Commit and same-phone installation handoff

- Android implementation: `6b86613` (native preview, source-backed project guides, host checks).
- Backend foundation: `0c9fea3` (disabled exam API, D1 schema, policy/schema tests).
- APK signature verification: Android SDK `apksigner verify --verbose` returned `Verifies`, v2 signature true, one signer. Debug signing remains development-only.
- Final lint: zero errors, one warning recommending legacy fullBackupContent configuration alongside modern extraction rules; allowBackup is false. No lint errors were suppressed.
- Copied verified APK to `/sdcard/Download/ZamCan-Academy-0.1.0-preview.apk`.
- `termux-open --view --content-type application/vnd.android.package-archive /sdcard/Download/ZamCan-Academy-0.1.0-preview.apk` exited 0. This establishes installer intent dispatch only. Installation and launch confirmation are still pending from the owner.
- Direct Android model/version/ABI queries returned Operation not permitted. No device identity or visual verification is fabricated.
- A later source-only edit removed one blank line's trailing whitespace after the successful build; it makes no semantic change to the verified APK.


## Continuation — 2026-11-15 / preview 0.3.0

Baseline: `37bb54e` merged with local checkpoint `5a3239b`, branch `preview/android-foundation`, including updated brand,
pedagogy, course sequence, foundation assets and independent CI jobs.
This entry supersedes conflicting historical status above.

### Done in this continuation

- Reworked home and course layout after the owner rejected 0.2.0 design and course organization. Separate journey, pathway selection and compact course maps replace the large single list. After merging newer instructions, home has Compiler / Learn C/C++ only, no publisher banner. Compiler opens the editor with files/projects/export in an overflow menu.
- Original light/dark PNG artwork is bundled and rendered on home/Academy; see ASSET_ORIGINS.md for saved resource paths and prompts. No HTML mockup was substituted for the native application.

- Required introduction now has a four-part offline practice checkpoint:
  multiple choice, true/false, fill-in and numbered ordering. The former
  button-tap `intro_completed` flag no longer awards mastery. Existing lesson
  completion records are preserved; the new introduction must be passed.
- Lesson phases: explanation, worked source, practice, checkpoint. Reading and feedback use the new native ReadingView; questions use quizScreen, one at a time with disabled-until-selected Check.
  Saved per-language resume, lesson stage, bookmarks and bounded private notes.
  Reading uses measured text pages with saved offsets; Back reviews prior pages. Correct
  checkpoint answers unlock Continue immediately. Notes do not grant mastery.
- Entry-point prerequisite and bounds checks also apply to resume/navigation.
- Saved light/midnight themes for background, surfaces, controls, editor and
  dialogs; reading text increased to 16sp. C/C++ CodeLab branding retained.
- LearningFeedback configures platform click sounds, a 130ms system-volume success tone and a 260ms fade/scale on first successful answers. Sound/motion toggles persist; success tone checks ringer/stream volume and animation checks ValueAnimator.areAnimatorsEnabled. Audible behavior, DND interaction and reduced-motion behavior are not device-verified.
- VersionCode 3 / versionName 0.3.0. Signed with the existing development key.
- Read-only same-phone package diagnostic and reproducible, checksum-pinned
  WebAssembly compiler research harness added.

### Evidence

- `tools/verify_android.sh --offline`: `testDebugUnitTest lintDebug assembleDebug`
  passed. Actual log: `verification/android-build.log`.
- JVM results: 15 tests, 0 failures,
  0 errors. New tests cover intro answers, prerequisites,
  invalid lesson indices and corrupt saved-stage values.
- Host checks passed: twenty lesson examples, ten project reference builds
  and exact transcripts, eight backend policy tests, six SQLite schema tests.
- Delivered APK: `/sdcard/Download/C-C-CodeLab-0.3.0-signed.apk`, 3481389 bytes.
  SHA-256: `10465acc5d2d1a16db940b3f7b53a0a04d22fbd55d5c56735afc35feab858b0c`.
- Signature schemes v2/v3 verified; ZIP alignment passed. Same-phone Android
  framework parsing and certificate verification passed. Evidence is recorded
  in `verification/package-verification.txt`. This does not establish installation.
- Device: Samsung SM-A055F, Android 15 / API35, arm64-v8a with 32-bit ARM ABIs.
  The previous 0.1.0 APK also passed framework signature/parsing checks despite
  the installer error; a missing signature was not established as its cause.
- Candidate compiler investigation: see `docs/TOOLCHAIN_CANDIDATE.md` and
  `verification/wasm-candidate-probe.log`. Actual Clang/LLD compilation and
  WebAssembly execution passed C11 standard libraries/input/math, C++17
  containers/templates/algorithms, RAII/smart pointers, multi-file C linking,
  virtual files, inaccessible host files, invalid-source diagnostics, nonzero
  exit status, and supervised infinite-loop termination. Missing compiler
  builtins initially caused a C++ link failure and were explicitly added.

### Partial, blocked and next work

- **Installation/UI: 0.3.0 pending owner test.** The owner reviewed 0.2.0 and rejected its design/course arrangement. Earlier 0.1.0 installation failed.
  Open the new APK through Files → Downloads; report the actual result. No
  successful launch, screenshots, accessibility or visual acceptance is claimed.
- **Compiler integration: blocked / research only.** The tested candidate is
  an old WebAssembly distribution, not an integrated Android runtime. No toolchain
  assets are bundled in 0.3.0. Complete licensing/maintenance review, bounded
  runner implementation and device matrix before exposing Compile/Run.
- **Learning: partial.** Twenty introductory lessons do not satisfy the required
  sixteen-module pathway per language. Deep traces/diagrams, search, spaced
  practice and full content remain. Selections are stored, but restoring exact quiz/feedback state after recreation remains incomplete; full timing/accuracy summary is not implemented.
- **Projects/editor: partial.** Ten-check preview project unlock remains, not
  full-course production eligibility. Import/rename/delete, undo/redo, bracket
  matching, background I/O and extensive editor lifecycle checks remain.
- **Cloudflare/certification/AdMob: incomplete.** Disabled backend scaffolding
  only; no deployed identity/exams/issuance/verifier, real credentials or ads.
- **Production readiness: not achieved.** Next: successful install/launch and
  device checklist; then Android compiler/library execution and full curriculum.


### Screenshot-state reconciliation (37bb54e requirements)

| Reference state | Implemented location | Evidence and remaining defect |
|---|---|---|
| 1. Reading presentation | ReadingView; MainActivity.readStory | Native light/dark page, measured text, segmented rail, fixed controls. Build evidence only; no phone screenshots or font-scale acceptance. |
| 2. Progressive explanation | ReadingView.paginate/showPage | Paragraph/sentence-preferred measured pagination and saved offsets. Authored within-beat bullet reveals remain incomplete. |
| 3. Section transition | foundation/programmingIntroduction/languageHistory | Sequential navigation exists; richer authored recap transitions remain. |
| 4. MCQ | quizScreen/introductionQuestion | Four-option intro MCQ, stored selection, Check disabled until ready, feedback. Existing course checks have two choices. Compact landscape/large-text testing pending. |
| 5. Fill blank | introductionQuestion index 2 | Reversible word choices and grading; inline replacement of the blank is not yet implemented. |
| 6. Feedback | quizScreen → readStory | Separate correct/incorrect explanation with review/retry. No fabricated results. Device review pending. |
| 7. Completion | LearningStore.complete; courseMap | Real checkpoints and a final foundation completion message persist/show actual completed counts. Aggregate accuracy/time summary remains not implemented. |
| 8. Continuity | ReadingView offset preferences; MainActivity restore | Course/reading offsets, notes, bookmarks and selections persist. Full process-death quiz/feedback and all intro routes require further work. |

New adaptive launcher foreground/background/monochrome assets were preserved from remote. Launcher mask verification remains pending. This table records partial implementation, not acceptance of all eight states.
## Midnight launch gate — 2026-11-15 (Africa/Dar_es_Salaam)
**Requested deadline:** 2026-11-16 00:00 EAT. This is a deadline, not a release certification. Do not publish a production claim merely to meet it.

**Latest verified source commit:** `b3b07b85c9bdb8d51488d72cf62be912d19c0978` — home CTA cleanup, Compiler file menu, quiz Check disabled until selection, shorter lesson labels. GitHub source commit is verified; Android compilation, UI screenshots and on-device testing of this change are **not yet verified**.

### Required immediate checks for active agent
1. Read latest `AGENTS.md` on `preview/android-foundation`, including screenshot-derived learning states; pull latest branch before editing. Coordinate concurrent writes, preserve existing commits and report conflicts.
2. Run `bash gradlew --no-daemon testDebugUnitTest assembleDebug lintDebug`; upload signed-off debug APK and SHA-256 from actual build; fix build failures before release. CI workflow uploads `codelab-debug-apk` if generated.
3. Install on Android phone and test launcher adaptive mask, minimal home, file drawer, saved sources, progress, lesson navigation, quiz selection and completion; capture screenshots.
4. **Block production launch** until real Clang C/C++ compilation/run, Cloudflare exams/credentials/verifier, AdMob consent/test banners, security/privacy and distribution signing are implemented and tested. If incomplete, label deliverable **limited offline learning/editor preview** and state exclusions prominently.
5. Publish no fabricated APK, screenshot, test logs, certificate, compiler results or deployment URLs. Do not substitute a source commit for a completed release.

### Remaining blockers at time of this entry
- Real on-device Clang/C++ toolchain and isolated execution: **not verified / absent in current app**.
- Cloudflare Worker deployed, live certificate issuer, signer, PDF, public verifier: **not verified**.
- AdMob SDK and consent flow: **not integrated in current app**.
- Fresh Android build and physical-device UI validation after the latest source change: **not verified**.


## Main consolidation and compiler integration — 2026-11-15

User explicitly requested main as the only branch. Local checkpoint `d3ebb26` is being merged with remote `a90c1df`; all remote compiler contracts, WAMR adapter, CI workflows, snippet models, tests and ad SDK scaffolding are retained. MainActivity keeps the measured native reader and actual bundled WebAssembly compiler, superseding the unprovisioned WAMR editor path. WAMR remains disabled pending Android native binaries/isolation. The remote rewarded-lesson lock is not activated because AGENTS requires all learning free and offline; rewarded ads must be optional. Existing local progress is preserved.

The Clang 22.1.8 / LLD release and WASI SDK 33 sysroot are now bundled (gzip compressed) with runtime SHA-256 validation and notices. Compilation and program execution run in a disposable Web Worker under a restricted local WebView origin; program WASI imports are checked, host files/network/Java bridges are unavailable. Programs use a fresh virtual filesystem. Limits: 32 files, 1 MiB project, 64 KiB stdin/output, 64 MiB program memory, 128 MiB virtual filesystem linear memory, 8 MiB virtual file, 16 MiB open write buffers, 120-second compile/link and 3-second execution watchdogs. Compiler renderer total memory still requires profiling/hardening.

Build before latest merge: unit/lint/assemble passed in 5m15s; APK SHA-256 025fa4528aba5b282805bc9d2fa8df237ab16ce59f27df6d69570584bfd78ace. This hash is historical, not the final merged APK. Packaged-engine tests are running. Node initially aborted with a host futex error after three passes; the failure log is retained and cases are rerun in separate processes. No Android WebView runtime or installation claim is made.


## Certification continuation — 2026-11-16

Base commit `ec551d45962995c446b52f67adfcd8e2a5576eef` is on main; prior feature history was merged before removing the preview branch. All new work remains on main. This entry supersedes earlier stale statements that PDF/signing/authentication source is absent; it does not establish deployment readiness.

- **Done locally:** passkey authentication, role/CSRF/session protections, D1 migrations, bilingual dashboard, server assessment, signed credentials, PDF/QR, status/revocation, name snapshots/reconfirmation and fixed 70% policy. Backend integration log: `verification/backend-integration-tests.log`, 19/19 tests passing, including genuine cryptographic passkey assertions, independent credential verification, PDF parsing, backup encryption/restore and name policy. `npm run build` and `npm run check` passed. These are host tests, not deployed tests.
- **Done locally:** compiler assets passed 14 host integration cases; Android 0.4.0 build/unit/lint/assemble succeeded. Signed APK placed in the phone Downloads folder. Package/signature acceptance is not proof of installation or on-device compilation; the requested six-case phone self-test has no reported result yet.
- **Partial:** Cloudflare configuration/dry-run and deployment tooling exist; `wrangler whoami` reports not authenticated. No live D1 binding or issuer secrets are configured. No production-readiness claim.
- **Partial:** existing selected-answer exams cover five supported grading styles. Practical isolated grading, remaining response types, full independently reviewed C/C++ question bank, 16-module-per-language content and five complete course-to-exam competency journeys remain unfinished. Target practical emphasis is documented, not falsely implemented.
- **Blocked externally:** live deployment needs account-owner OAuth completion, database provisioning and protected issuer-key configuration. Continue preparation locally meanwhile.
- **Not completed:** Android certification synchronization, AdMob consent/banner integration, real browser/PDF visual QA, physical-phone UI/accessibility QA, deployed load/security/backup tests and independent credential interoperability certification.

User policy is now authoritative: three official names before examination; pass >=70%; explicit post-pass name confirmation before certificate generation. Verified name mismatches fail closed and need academic identity review. The dashboard autosave failure tracking now retains failures per question so a later successful save cannot hide an earlier unsaved answer.


## Cloudflare account verification — 2026-11-16

- **Done:** owner-approved Wrangler OAuth completed; `npx wrangler whoami` confirmed membership in `Zamcannexus@gmail.com's Account` (the requested ZamCan Nexus account). Configuration explicitly selects account `4603d64520de2639996e13d44985f4fa`; no OAuth token is recorded here or in source.
- **Done:** existing D1 `zamcan-codelab-certification`, ID `c3efb3e2-9e4d-42a4-89f8-9a54016c2978`, is bound in `backend/wrangler.jsonc`. Remote read-only inspection reports zero rows written. `verification/cloudflare-existing-schema.json` contains schema definitions only. `python3 tools/check_existing_d1.py` confirms the original migration's tables/indexes match.
- **Important migration gate:** the remote database has the original tables but no `d1_migrations` table. Do not replay migration 0001. Obtain a protected backup and establish the matching baseline history before applying only 0002/0003. No remote migration was applied in this session.
- **Observed:** the planned Worker name is absent (Cloudflare code 10007); no existing Worker was overwritten. The public origin remains a placeholder until the account's actual Worker hostname is established.
- **Runtime blocker:** local Wrangler D1 migration execution failed in Android/PRoot workerd with TCMalloc virtual-address allocation failure and EPIPE. This is not a passing D1 runtime test. SQLite integration tests remain separate evidence. CI now includes local D1 migration execution on Ubuntu for the real runtime check.
- **Still disabled:** authentication, examinations and certificate issuance flags. No real certificates issued, student records modified, private keys provisioned, or production readiness asserted.
