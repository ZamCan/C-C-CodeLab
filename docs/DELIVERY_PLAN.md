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
