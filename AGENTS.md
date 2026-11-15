# C/C++ CodeLab — master engineering directive for all coding agents

**Repository:** ZamCan/C-C-CodeLab. **Product:** C/C++ CodeLab. **Publisher/issuer:** ZamCan Academy (subtle, context-specific). **Priority:** Android first; reusable engines for additional academic subjects. **Target milestone:** Monday 2026-11-16 for an installable *tested preview*, not an unverified claim of production readiness.

## Binding product decisions
1. After a brief polished wordmark/boot transition, show exactly two primary home destinations: **Compiler** and **Learn C/C++**. Achievements, settings, and certificates are secondary navigation.
2. All learning materials, practice, flash questions, ordinary quizzes, coding exercises, guided projects, and Compiler usage are **free and offline-first**. Certificate final exams, trusted issuance, and live status checks require internet.
3. C and C++ are the first complete learning tracks; architecture must allow mathematics, science, languages, and other academic subjects without duplicating learning, assessment, or credential engines.
4. Learners retain downloaded PDF and signed digital credentials. Cloudflare hosts a minimal online exam/issuance/status/verifier service. No mandatory commercial certificate partner or pay-per-certificate service.
5. Exactly five post-course guided C/C++ projects: calculator, educational login/user management, student results, fictional ATM, library management. Unlock project guidance after the long course journey; certification is a separate optional online path.
6. Project previews are explicitly labeled **static interface demonstrations**. Live program output must come from actual C/C++ compilation/execution, not fabricated previews. Do not claim that this application produces Android APKs or arbitrary GUI programs.
7. Build on existing repo code; inspect and preserve working code, assets, logos, designs, tests, and Git history. Do not replace superior existing implementation with a mockup. Never remove features without evidence and justification.

## Source-of-truth reading order
- docs/PRODUCT_SPEC.md — UX, learning progression, curriculum, project lab
- docs/COMPILER_ENGINE.md — Android compiler/editor/terminal and security
- docs/CERTIFICATION_ENGINE.md — exams, Cloudflare, credentials, backups
- docs/DELIVERY_PLAN.md — milestones, verification, acceptance gates
- docs/ARCHITECTURE.md, docs/EXAM_POLICY.md, docs/PROJECTS.md — earlier decisions; reconcile rather than silently override

## Academic pedagogy — non-negotiable
- Teach from first principles with university-quality precision but beginner-friendly language. Every unfamiliar token gets its **name**, **meaning**, **reason for placement**, **what changes if moved or removed**, **language/version constraints**, **common mistakes**, and **a small test**.
- For `#include`, angle brackets, semicolons, braces, parentheses, `int main(void)` vs `int main()`, `printf`, `std::cout`, namespaces, headers, variables, types, pointers and references, explain **why**, not just what.
- Teach correct C and C++ differences; avoid confusing C syntax with C++ syntax. Explain C development c. 1969–1972 and C++ beginnings as C with Classes in 1979, with historical nuance.
- Progression: intuitive problem → algorithm/pseudocode/flowchart/decision table → working code → token-by-token anatomy → compile → diagnostic → experiment → recall question → exercise → independent solution.
- Worked examples include interest on an initial 1000 at 10% annually until amount > 2000, Celsius to Fahrenheit, number systems, grade calculations, input validation and branching.
- Teach clean code formatting: four-space indentation by default, consistent brace placement, whitespace around operators, sensible naming, syntax highlighting with accessible dark/light contrast, matching braces, line numbers, and clear diagnostics. Explain indentation is typically for readability in C/C++, while braces and syntax control scope; do not teach Python-style indentation semantics for C/C++.
- Author original explanations; use K&R, Stroustrup, relevant ISO standards and other credible references as research anchors. Do not paste copyrighted book chapters or imply endorsement by publishers.

## Agent operating protocol
1. Inspect repository tree, current Android project, GitHub Actions and build configuration before modifying.
2. Maintain a status ledger in docs/DELIVERY_PLAN.md: done / partial / blocked / not started, with concrete evidence and commit IDs.
3. Prefer vertical slices: first boot/home, persistent editor, real C compile/run, real C++ compile/run, one complete lesson, one full guided project, then expand.
4. Keep interfaces and engines separated; Kotlin + Jetpack Compose are preferred for native Android if the repo does not already establish a working alternative.
5. Add unit, integration, and Android device tests; capture exact commands, logs, stdout/stderr, exit codes and artifacts.
6. Keep all private keys, tokens, credentials, API secrets and real student data out of Git. Do not expose answer keys in APK.
7. Do not claim success based on a static mockup, CI configuration, Linux-host build, or a generated file alone.
8. Produce an installable debug APK and checksum only after actual build succeeds. Never fabricate download links, test reports, certificates or accreditation.
9. Avoid forced monetization, lesson paywalls and blocking ads; optional restrained banners only, with privacy compliance and free core learning.
10. Keep app usable on modest Android devices; optimize offline assets and storage. Add Swahili and English localization scaffolding; make future Arabic/RTL possible.
11. Before any destructive migration, backup existing code and preserve working versions. Use small, reviewable commits.
12. If requirements conflict or are physically impossible (e.g., preventing force-close or guaranteeing a 1% repeat probability forever with finite questions), implement the secure achievable interpretation, document limitations and never mislead.

## Definition of ready
No production-ready claim without device-verified offline C/C++ compilation, editor/project persistence, tested learning content, secure exam backend, trusted credential issuance, real public verification, threat-model checks, accessibility, backup/restore evidence, and repeatable Android build. A preview milestone may ship earlier with limitations prominently stated.

## Mandatory premium UX and compiler correction — 2026-11-15 (supersedes conflicting wording above)
- **Product name:** C/C++ CodeLab; discreet publisher attribution **by ZamCan Academy**. This is not a generic academy homepage. The two primary entry labels are **Compiler** and **Learn C/C++**. Do not use **Code Studio**, **Studio**, **Learning Academy**, or long CTA slogans as primary navigation labels. Keep language direct and meaningful to students.
- **Learning screens:** white/light is the reference-first theme, with an equally polished optional dark theme. Match the attached reference screenshots' principles: generous white space, dark legible typography, top segmented progress, restrained blue-to-violet gradient primary action, precise alignment, compact icon actions, quiet animations and clear success/checkpoint feedback. Do not copy illustrations or content. Ensure accessible contrast, touch targets, dynamic text sizing and Android insets.
- **Microcopy:** primary lesson controls use one or two words: **Next**, **Back**, **Try**, **Check**, **Continue**, **Finish**. Never use verbose instructional CTAs such as "Continue explanation", "Explore the academy", "Next: knowledge check", or marketing prose where a direct verb suffices. Academic depth belongs inside the lesson, not on the button.
- **Professor-quality teaching:** introduce an idea, reveal the next logically dependent explanation, use an original precise diagram or code example when useful, invite an experiment, check understanding, explain feedback, then advance. Content must be substantive, original, technically correct and accessible to first-year computer science learners. Do not enforce a slow tap for every sentence; chunk by concept and provide navigation and resume.
- **Home architecture:** minimal two-destination home, not a stack of promotional cards. **Compiler** opens the actual source editor/compiler workspace. **Learn C/C++** opens courses. Do not let secondary menus compete with these destinations.
- **Compiler layout:** source editor, language selection (C / C++), compile/run, real stdin/stdout/stderr, diagnostics, exit status and save state are first-class. Project files, new/open/rename/export, settings and secondary utilities belong in an overflow **⋮** menu or collapsible file drawer, not an always-visible list of giant cards. Files must remain discoverable, accessible and safely persisted; never silently hide user data.
- **Compiler engine is a release gate:** provide Android-compatible real C and C++ toolchains, standard headers/libraries, correct linker and runtime packages, versioned compiler assets, target-ABI handling, deterministic diagnostics and safe process execution with resource/time limits. Validate actual compilation and execution of C and C++ with stdin, stdout, stderr, exit code and common standard-library programs on supported Android ABIs. Avoid assuming Termux's compiler can be silently bundled or that glibc Linux binaries execute on Android; handle Android Bionic/NDK compatibility and licenses. Clearly report unavailable compilers rather than fake outputs. Do not call a text editor a working compiler.
- **Implementation and QA:** preserve existing Java/native code unless migration is justified; inspect the app and replace the current verbose cards/navigation progressively. Add screenshots/tests for small phones, light/dark modes, progress, menu/file discovery, editor persistence, real compiler behavior and accessibility. All agents must read this section before changing screens. Build, run and verify before claiming APK readiness.

## Publisher identity, integrations and launcher acceptance — 2026-11-15
- Do **not** place “BY ZAMCAN ACADEMY”, “by ZamCan Academy” or repeated publisher banners on the home screen, lesson headers or navigation. Use **ZamCan Academy** sparingly in About, selected course metadata, and certificates as the issuer when legally and factually accurate. Primary identity remains **C/C++ CodeLab**.
- Launcher: supply adaptive foreground/background resources and a monochrome layer, using a legible C/C++ mark inside Android's central safe zone. Verify circle, squircle and rounded-square masks and small sizes; never put lettering near cropped edges. Test on real launchers; do not claim this verification without screenshots.
- **Clang/LLVM**: plan and implement ABI-specific Android-compatible compiler/toolchain distribution with Clang, lld, C headers, C++ standard library headers and runtime, licensing, signatures/checksums, storage constraints and compiler diagnostics. Check actual compilation and execution of C and C++ on devices. Do not mistake a compiler requirement document for installed binaries.
- **Cloudflare**: complete Workers + D1 deployment, authenticated exam sessions, server-side question selection/grading, time limits, rate limits, durable attempts, verifiable signed credentials, PDF output, public verification/revocation, and security/backup testing. Keep signing keys server-side; don't mark issuance complete until verified end-to-end.
- **AdMob**: implement privacy-aware, consent-aware anchored adaptive banners only, never interrupting editor, terminal, quiz choices, navigation or accessibility. Use test ad units for development; configure production IDs securely before release; verify platform policies and regional consent requirements.
- Keep the interface eye-friendly and engaging through typography, whitespace, contrast, subtle progress/feedback motion and responsive layout, not excessive panels or promotional copy.
