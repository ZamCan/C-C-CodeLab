# ZamCan Academy architecture

Existing repository: ZamCan/C-C-CodeLab. C and C++ are the first course tracks; future subjects reuse the learning, assessment and credential interfaces.

## Android home
1. **Code Studio** — unlimited offline C/C++ practice, multi-file projects, local persistence, editor and terminal.
2. **Learning Academy** — lessons, examples, exercises, flash questions, quizzes, guided projects.
3. Achievements are accessible from the academy, not a third competing primary home section.

## Execution safety
Never simulate compiler success. The Android compiler requires an Android-compatible packaged toolchain, linker, C/C++ runtime, verified subprocess execution, memory/time/output limits, per-project isolation and termination. Host Clang tests are not Android proof. Native student code shares the app sandbox unless stronger isolation is built; do not claim OS-grade containment without testing.

## Course design
Ten core C lessons and ten core C++ lessons; language origins, nuanced comparisons, interoperability using extern "C", syntax anatomy, indentation, headers and libraries, program design and debugging. Original explanations grounded in authoritative books; short attributed quotations only.

## Five guided capstones
1. Calculator
2. Login system (educational; no plaintext password persistence)
3. Student results management
4. ATM simulator (fictional balances only)
5. Library management system

Unlock guided projects on course completion, independent of purchasing or obtaining a certificate. Static interface previews must be explicitly labeled and must never be mistaken for actual compiled program output.

## Certification
Free offline learning; online final exams and authorized certificate issuance. Cloudflare Workers + D1 planned. Learners own downloadable PDF and signed credential. Private signing keys never ship in APK or GitHub. Public verification checks signature, issuer key and status. No accreditation or partnership claims.

## Release gates
Build APK, test on real Android device, prove offline compilation for C and C++, stdin/stdout/stderr/exit status, test safety limits, offline persistence, exams, issuance, verifier, accessibility, and AdMob test ads before any production claim.
