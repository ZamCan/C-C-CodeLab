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
