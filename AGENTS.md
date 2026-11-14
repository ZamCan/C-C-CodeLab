# ZamCan Academy — master engineering directive for all coding agents

**Repository:** ZamCan/C-C-CodeLab. **Product:** ZamCan Academy (the continuation of CodeLab C & C++, not a separate competing application). **Priority:** Android first; reusable engines for additional academic subjects. **Target milestone:** Monday 2026-11-16 for an installable *tested preview*, not an unverified claim of production readiness.

## Binding product decisions
1. After a brief polished wordmark/boot transition, show exactly two primary home destinations: **Code Studio** and **Learning Academy**. Achievements, settings, and certificates are secondary navigation.
2. All learning materials, practice, flash questions, ordinary quizzes, coding exercises, guided projects, and Code Studio usage are **free and offline-first**. Certificate final exams, trusted issuance, and live status checks require internet.
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
