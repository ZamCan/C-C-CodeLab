# Engineering ledger — learning experience rebuild (2026-11-26)

Branch: `main`. Scope: the native learning experience, boot/landing identity and lesson authoring.
The compiler packaging, evaluation backend and certificate service were verified but not modified.

## Completed

| Area | Status | Evidence |
| --- | --- | --- |
| Unified design tokens (dark-first + light) | done | `Design.java`; lint clean; used by all new surfaces |
| Original boot/landing artwork and flow | done | `BrandSplashActivity`, `WelcomeActivity`, `MeshBackdrop`; local build succeeds |
| Guest-first landing with optional Google action | done | `WelcomeActivity`; previous verified learner enters directly |
| Beat-based professor-led lesson engine | done | `Beat`, `BeatPlayer`; Back/Next continue the current narrative; scored checkpoints gate Next |
| Durable lesson position, answers, bookmarks | done | preference keys per lesson in `MainActivity.lesson(int)`; resume on relaunch |
| Completion reports real accuracy and elapsed time | done | `MainActivity.completeLesson(...)` |
| 12 C + 11 C++ authored lessons | done | `LessonScript.java`, `LessonScriptCpp.java`; 23 beats-driven lessons |
| Original Canvas instructional diagrams | done | `ConceptArt.java`, 12 concept families |
| Every example verified by a real compiler | done | `tools/verify_curriculum_examples.py`: 23 compiled/executed, 0 failures |
| Every example verified through the app's own Clang/lld/WASI engine | done | `node tools/test_packaged_compiler.cjs --curriculum`: 23 compiled/linked/run, 0 failures |
| Teaching `iostream` subset documented | done | `docs/COMPILER_ENGINE.md` "Measured runtime limits"; streams lesson corrected to the verified subset |
| Lesson-integrity tests | done | `LessonScriptTest`; 72 unit tests total, 0 failures |
| Stale CompileLearn branding removed | done | launcher label `C/C++ CodeLab`; unused `BrandHero` deleted |
| CI compiles the authored examples | done | `.github/workflows/android.yml` lessons job |

## Commands and results

```
./gradlew :app:testDebugUnitTest   -> 72 tests, 0 failures
./gradlew :app:lintDebug           -> success
./gradlew :app:assembleDebug       -> app-debug.apk (44.95 MB)
python3 tools/verify_curriculum_examples.py -> 23 examples, 0 failures
python3 tools/verify_toolchain.py --apk app/build/outputs/apk/debug/app-debug.apk -> all PASS
node tools/test_packaged_compiler.cjs              -> 14 packaged-engine checks, all PASS
node tools/test_packaged_compiler.cjs --curriculum -> 23 lesson examples, all PASS
```

## Not verified / remaining manual work

- No Android device or emulator is available in this environment: the new screens have not been
  launched, clicked through or screenshotted on hardware, and TalkBack/large-font checks are
  outstanding. CI's instrumentation job remains the gate for on-device compiler behaviour.
- Google OAuth still requires the Appwrite/Google redirect configuration owned by the project.
- Certificate examinations/issuance, AdMob production IDs and the Cloudflare deployment are
  unchanged and remain behind their existing safety gates.
- Guided project briefs and the legacy foundation reading flow still use `ReadingView`; they are
  intentionally unchanged in this pass.
