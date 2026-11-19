# Compile • Learn C/C++

**In-app brand:** CodeLab — C/C++ Compiler & Learning Academy. **Android launcher:** Compile • Learn C/C++. **Intended store listing title:** Compile • Learn C/C++ (must be set in each publisher console; repository edits cannot change a live store listing). **Package:** `com.zamcan.codelab` (unchanged).

Offline-first Android academy with two primary home destinations: **Compiler** and **Learn C/C++**. C and C++ are the initial subjects; shared learning, assessment and credential engines are designed for additional academic subjects.

## Source availability — important for agents
**There is no verified, accessible full Android source prototype to import from this repository.** An earlier README incorrectly suggested that a complete prototype was being prepared for import. No path, archive, commit or download for such a prototype has been established. **Do not wait for it.** Continue implementing the Android app in this repository, preserving any existing working files found through repository inspection.

**No integrated Android offline C/C++ compiler or bundled toolchain has been verified.** The `poc/hello.c`, `poc/hello.cpp`, and `.github/workflows/host-poc.yml` files demonstrate host-Linux C/C++ smoke tests only. They are not an Android compiler, native runtime or device build.

**No deployed Cloudflare exam, certificate signing, storage, or public verification backend has been established or verified.** The Cloudflare backend is a specification to implement, not an existing service or accessible deployment. Never invent Cloudflare account IDs, keys, URLs, or credentials.

## Agent entry points
1. Read [AGENTS.md](AGENTS.md) for binding requirements and working protocol.
2. Read [product specification](docs/PRODUCT_SPEC.md), [compiler engine](docs/COMPILER_ENGINE.md), [certification engine](docs/CERTIFICATION_ENGINE.md), and [delivery ledger](docs/DELIVERY_PLAN.md).
3. Inspect actual repository code and Git history, then build a real Android vertical slice; document all tests and remaining blockers.

## Non-negotiable product commitments
- Free offline lessons, exercises, flash questions, quizzes, projects and unlimited coding practice.
- Real offline C/C++ compilation/execution with honest diagnostics, not mocked compiler output.
- Five guided projects after the main learning journey: calculator, educational login, student results, fictional ATM and library management.
- Online randomized final examinations and learner-owned signed credentials, with Cloudflare as planned infrastructure.
- A tested Android APK must precede any 'ready' claim.

## Current verified scope
Native Android previews have built and passed unit/lint checks. Package signing/parsing is verified; full device functionality, integrated offline compilation and deployed certification remain unverified. See the delivery ledger for exact artifacts and limits.

## Android preview implementation (2026-11-14)

Native Java Android source is now in `app/`, with a dark glass-style UI, two home destinations, persistent project/source editing, SQLite learning progress, introductory C/C++ lessons, detailed syntax and math notes, and five project guides with starter/reference files. The initially available Java implementation is retained while engine boundaries are improved; Compose remains a future option, not a reason to discard the working build.

Build: `./gradlew testDebugUnitTest lintDebug assembleDebug`. Host teaching checks: `python3 tools/verify_lessons.py` and `python3 tools/verify_projects.py`. Backend policy tests: `node --test backend/test/*.test.mjs`; schema tests: `python3 tools/test_backend_schema.py`.

The Cloudflare foundation in `backend/` is undeployed and disabled by default. Native Android C/C++ execution, live certification, visual/device validation, and several advanced editor/course features remain open. See [delivery evidence](docs/DELIVERY_PLAN.md) and [compiler investigation](docs/COMPILER_RESEARCH.md). This is not a production-readiness claim.
