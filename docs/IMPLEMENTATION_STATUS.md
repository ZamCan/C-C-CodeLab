# Android implementation and release gates

This repository now contains a native Android development app. It is **not production-ready**. The presence of an APK does not establish completion of the architecture specification.

## Implemented

- Native Android Java application, Android 8.0+ (API 26), targeting API 35.
- Dark aurora background and translucent gradient panels, outlined rounded controls, touch ripples, mint/violet accents. This is a glass-inspired treatment, not a real-time backdrop blur implementation.
- Code Studio and Learning Academy navigation; achievements live within the academy.
- Local UTF-8 source workspace supporting C, C++, and headers. Multiple files share one workspace; independent project directories and project-wide builds remain pending.
- Source editing, basic lexical highlighting, adjustable text size, debounced save, explicit save, and Storage Access Framework export.
- Atomic source replacement, filename validation, 256 KB source limit, and explicit save errors.
- Ten introductory lessons per language, worked source examples, exercises, and offline practice checks with explanations.
- Local course progress and project-brief unlocking without payment or certification.
- Build-status disclosure; no fabricated execution output or certificate issuance.
- No network permission, advertising SDK, analytics, or credential collection.

## Required before production

1. Import or implement the Android-native compiler/runtime. Verify ABI support, headers, C/C++ libraries, linking, stdin/stdout/stderr, status codes, cancellation, time/memory/output limits, and isolation. Android restrictions on executing generated binaries require an explicit tested design, not a host compiler copied into assets.
2. Extend source workspace into independently isolated multi-file projects, with import, file management, build configuration, recovery, and tested background I/O for large workspaces.
3. Expand the introductory curriculum into reviewed full teaching material, flash questions, automatic coding assessment, and ten complete C/C++ capstone implementations with starters, solutions, tests, sample I/O, and progressive walkthroughs. Current capstones are briefs only.
4. Implement authenticated online exams, tagged question-bank selection, server-authoritative deadlines, exposure policy, accommodations, connectivity/review policy, and dedicated isolated native grading. Never embed hidden exam questions or answer keys in the APK.
5. Implement authorized idempotent credential issuance, protected signing keys, downloadable PDF/signed credential, revocation/status records, and public verification. Practice completion is not a credential.
6. If advertising is enabled, implement consent and test ads, then verify behavior and privacy declarations. Advertising is currently absent.
7. Test on physical Android devices: smallest supported screens, landscape, large fonts, TalkBack, keyboard/IME, process recreation, low storage, failed document-provider exports, and long editing sessions. Check contrast and interactive states visually. No device was connected during initial implementation.
8. Configure owner-controlled release signing, dependency/security review, privacy/store declarations, distribution policy, and release update testing. Debug signing is development-only.

## Local verification

Run `./gradlew testDebugUnitTest lintDebug assembleDebug`. The GitHub Actions workflow runs these tasks and uploads a development APK when pushed. Workflow configuration is not evidence that remote CI has passed.

The local Termux environment uses an SDK and AAPT2 override outside the repository. `local.properties` is ignored; ordinary checkouts must configure their own Android SDK.

Source-store tests cover Unicode/restart persistence, traversal rejection, atomic replacement, oversize write preservation, and byte-based limits. These host tests do not replace device testing.

## Later changes in this session

The current delivery ledger supersedes the initial inventory above: isolated project directories, line numbers/indentation, SQLite progress migration, English/Swahili scaffolding, detailed syntax/math notes, five source-backed guides, and a disabled Cloudflare exam foundation have since been added. Full native execution, deep curricula, advanced editor features, physical-device verification, and trusted credential issuance are still incomplete.
