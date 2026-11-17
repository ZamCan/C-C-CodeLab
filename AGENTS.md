> **UNIFIED SIGN-IN CONTRACT (2026-11-17):** Read [docs/UNIFIED_IDENTITY_CLOUDFLARE_BRIDGE.md](docs/UNIFIED_IDENTITY_CLOUDFLARE_BRIDGE.md). Google/Appwrite login is CodeLab-wide identity and MUST securely authenticate Cloudflare exam/certificate APIs via server-verified token exchange and canonical learner mapping. Never trust email/client claims, never expose secrets, and never use synced progress alone as certification proof. The bridge is specified, not yet implemented.\n\n> **MANDATORY MODERNIZATION HANDOFF (2026-11-17):** Before changing code, read [docs/AGENT_MODERNIZATION_PLAYBOOK.md](docs/AGENT_MODERNIZATION_PLAYBOOK.md) and [docs/HYBRID_AUTH_STORAGE.md](docs/HYBRID_AUTH_STORAGE.md). These are the current multi-agent implementation queue and launch architecture. Guest/SQLite is default; Google/Appwrite is the first optional online login; Amazon/Cognito/DynamoDB is disabled pending AWS activation; Cloudflare examination/certification remains separately verified and fail-closed. Preserve the existing native Java app and assets, implement rather than merely describe, and never claim release readiness without device tests. This instruction supersedes older conflicting deadlines, cloud assumptions, and provider decisions in this file.\n\n# C/C++ CodeLab — master engineering directive for all coding agents

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

## Mandatory screenshot-based learning experience — binding implementation brief (2026-11-15)
**Applies to every active/future coding agent. Read before modifying UI, curriculum, assessments or build scripts.** The user supplied consecutive screenshots of a C++ learning flow. They are **behavioral and visual references, not permission to copy copyrighted art, mascot characters, text, branding or illustrations**. Implement the same class of learner experience with original CodeLab assets and accurate university-level content.

### Observed reference states and required equivalents
1. **Lesson presentation:** white/off-white canvas, restrained charcoal text, a slim segmented blue-to-violet progress rail near the top, a close/back control, optional contextual share and illustration expansion, a centered short section title, an original compact visual when helpful, and clean left-aligned explanation blocks. Large quiet margins and ample whitespace; never crowd with web-style cards.
2. **Progressive explanations:** the supplied screenshots show successive additions to a lesson paragraph while the progress rail remains stable. Implement concept-by-concept disclosure with one- or two-word **Next** action; do not require one tap per sentence, and allow learners to review prior concepts. Provide resume and screen-reader announcements without forced typewriter effects.
3. **Section transition:** a short, encouraging original message and **Next**; no patronizing “buddy” tone, generic “see you” filler or compulsory illustration. A transition should motivate and accurately summarize what was learned.
4. **Multiple-choice knowledge check:** short question, optional “Select one answer” hint, four accessible selectable options with a clear selected state, and a fixed-bottom **Check** action disabled until a selection exists. A correct answer displays meaningful feedback and a concise explanation; an incorrect answer explains the misconception and permits appropriate retry/review.
5. **Fill-in-the-blank check:** a sentence with a genuine blank and tappable word choices; selected choice fills the blank and is reversible before **Check**. Ensure correct grammar and accurate programming terminology.
6. **Feedback state:** clear success/error indicator and original illustration or simple vector, readable explanation and **Next**. Do not copy the supplied orange character/mascot or use confetti as a substitute for feedback.
7. **Topic completion:** original restrained celebratory treatment; show **accuracy calculated from actual correct/attempted answers** and **elapsed time measured from the actual session**. No fabricated “100%” or “6m 34s”. Show the real next topic and **Next** / **Start** action, with persistent progress.
8. **Continuity:** the supplied images show a topic completion followed by an introduction to the next C++ topic and multi-step explanations. Preserve this navigation chain and real progress persistence across process death.

### Screenshot-specific academic corrections
- The screenshot quiz “Who created C++?” is correctly answered **Bjarne Stroustrup**; **Dennis Ritchie** is associated with C, **James Gosling** with Java, **Guido van Rossum** with Python. Teach this distinction.
- Explain that C++ was initially developed as **C with Classes**, not that its modern definition is simply “C with Classes.” Avoid the unqualified claim that C++ is only object-oriented: it supports procedural, generic, object-oriented and other paradigms.
- “Middle-level language” is an informal pedagogical label, not an official ISO language classification. Prefer precise discussion of high-level abstractions and low-level systems capabilities.
- Correct grammar and typos from the references; do not replicate “C++ is also called as”, “large areas” misspellings, or overly casual filler. Include a concrete code example and a short diagnostic experiment where appropriate.

### Engineering checklist — required real work, not documentation-only
- [ ] Implement responsive native Android lesson renderer for concept steps, progress rail, navigation, original illustrations, and persistent resume.
- [ ] Implement selectable MCQ, reversible fill-in-the-blank, disabled-until-ready **Check**, correctness evaluation, substantive explanations and retry/review.
- [ ] Implement accurate timing and score aggregation, durable topic-completion screen, next-topic routing, and accessibility/dark-mode equivalents.
- [ ] Reduce home to **Compiler** and **Learn C/C++**; remove conspicuous publisher branding, verbose CTA text, clutter and marketing prose.
- [ ] Build a genuine Android-compatible Clang/LLVM C/C++ toolchain with standard headers/libraries, ABI support, executable runtime and transparent diagnostics; prove real device compilation and execution.
- [ ] Complete Cloudflare Workers/D1 authenticated exams, secure server-side assessment, verifiable certificate issuance/revocation and tested verifier; keep keys out of client/repo.
- [ ] Add policy-compliant AdMob anchored adaptive banner with consent and test IDs; never obstruct compiler or learning interactions.
- [ ] Verify adaptive icon on circular and other launcher masks and confirm original mark is not cropped.
- [ ] Run Android build/tests and report evidence. Do **not** claim these unchecked tasks are complete. Keep a status ledger and check off only verified results.

**Agent handoff rule:** Before changing any UI or claiming readiness, compare each of the eight screenshot-derived states above to the actual app. Record implemented class/method, screenshot or test evidence, remaining defects, and commit in docs/DELIVERY_PLAN.md. Do not treat AGENTS.md updates as UI implementation. Preserve all earlier technical/security requirements.


## Codex Cloudflare handoff — 2026-11-15
Before implementing or deploying the examination and certification engine, read [docs/CODEX_CLOUDFLARE_HANDOFF.md](docs/CODEX_CLOUDFLARE_HANDOFF.md). It specifies the authorized ZamCan Nexus account, existing D1 resources, ten exam variants per course, exactly 25 top-level questions per attempt, multipart question counting, time-weighted assessments, safety gates and independent Termux authentication. This handoff is binding for Codex and other coding agents; no token or signing-key sharing is authorized.


## Deep curriculum and visual reference requirements — 2026-11-15
Read [docs/DEEP_CURRICULUM_AND_LEARNING_FLOW.md](docs/DEEP_CURRICULUM_AND_LEARNING_FLOW.md) before modifying learning screens, lesson assets, topic structure or assessments. Each topic must contain 3–5 substantive modules with tested C/C++ examples, syntax anatomy, diagnostics, experiments, exercises and mastery checks. The seven user screenshots are behavioral/layout references only; use original assets and copy. Course completion must not falsely imply trusted certificate issuance.


## Professor-led strings/types instruction — 2026-11-15
Read [docs/PROFESSOR_LED_TYPES_STRINGS.md](docs/PROFESSOR_LED_TYPES_STRINGS.md) before writing any lesson scripts, character illustrations, data-type or string modules. Use a consistent original CodeLab Professor, substantive learner-facing dialogue, pedagogically meaningful original diagrams, exhaustive accurate C17/C++17 type/constant/string coverage and real verified code examples. Do not impersonate a real educator or copy third-party artwork.
