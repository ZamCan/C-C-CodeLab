# CodeLab design system and beat-based lessons

**Status:** implemented on `main` (see the ledger entry in `docs/DELIVERY_PLAN.md` for commands,
outputs and remaining limits). This document explains what exists in code, so another engineer can
extend it without guessing.

## 1. Design tokens — `Design.java`

Dark is the primary appearance, light is fully supported:

| Token | Dark | Light |
| --- | --- | --- |
| canvas | `#05090F` midnight navy | `#F5F8FD` |
| surface | `#0C1524` | `#FFFFFF` |
| line | `#223049` | `#DBE4F1` |
| ink | `#EEF4FF` | `#0D1A2C` |
| muted | `#9DB0CA` | `#4C5D75` |
| accent | `#62E2C2` | `#0B7D69` |
| accent soft | `#123B36` | `#DEF5EF` |
| amber (milestones) | `#FFCF7A` | `#8A5A00` |
| code surface | `#081120` | `#F1F6FD` |

Spacing is an 8 dp rhythm; controls use 48 dp minimum touch targets and 54 dp primary buttons.
Roles are semantic (`ink`, `muted`, `line`, `accent`), never raw hex at call sites, so appearance
changes do not require a screen-by-screen edit.

## 2. Boot and landing — `BrandSplashActivity`, `WelcomeActivity`, `MeshBackdrop`

- The launcher activity draws an original Canvas mesh: a receding perspective grid behind a cool
  aurora sweep, with a bottom vignette so full-width actions stay readable. It is static (no
  per-frame timers or allocations), so cost is one layout and one draw.
- The boot lockup fades and scales in over 360 ms and routes immediately. With system animations
  disabled the transition is 120 ms and the same destination is reached — there is no artificial
  delay, no second welcome screen and no progress bar for local work.
- The landing screen carries the brand, one sentence of purpose, the branded Google action and a
  visible **Continue as guest** button. Guest progress is disclosed as device-local. The overflow
  menu holds About/version. A learner whose account was previously verified goes straight to the
  workspace.
- The Google button keeps the existing Appwrite browser OAuth flow; it does not add a parallel
  identity system.

## 3. The lesson engine — `Beat`, `LessonScript`, `LessonScriptCpp`, `BeatPlayer`, `ConceptArt`

A lesson is a sequence of **beats**, not a scrolling page. `Beat.Kind` covers:

`HOOK → DIALOGUE → DEFINITION → EXPLAIN → VISUAL → CODE → PREDICT/CHECK → TRY → REFLECT`

Rules enforced by `app/src/test/java/com/zamcan/codelab/LessonScriptTest.java`:

- every lesson has a hook, a precise definition, an explanation, a verified example, a hands-on
  `TRY`, at least one scored checkpoint and a recap;
- dialogue names its speaker and is explicitly labelled *imagined dialogue* — dramatised history is
  never presented as a quotation;
- every checkpoint has at least two distinct options, a valid answer index and an explanation;
- lesson ids are stable and unique per track;
- **Next continues the current narrative.** A scored beat disables Next until the learner answers;
  a wrong first attempt is recorded honestly, explains the misconception and requires a second,
  correct choice to continue.
- `Back` moves one beat backwards; the progress rail shows the full beat count.
- Progress is durable: current beat, answered checkpoints and bookmarks are stored per learner via
  `MainActivity`'s preference scope, so process death, rotation and leaving the lesson resume in
  place.
- Completion reports the **measured** first-attempt accuracy and elapsed time. Nothing is
  hardcoded.

The `TRY` and `CODE` beats can open the bundled example in the real compiler (`openScratchWith`),
which is the link between the academic explanation and executed code.

### Curriculum

| Track | Lessons |
| --- | --- |
| C | Meet C · Your first program · Variables and types · Input and arithmetic · Decisions · Loops and tracing · Functions and the call stack · Arrays and strings · Pointers · Structures · Files and errors · Testing and debugging |
| C++ | Meet C++ · Streams and the first program · Types and initialization · References and functions · Classes and invariants · Strings and vectors · Algorithms · Inheritance and polymorphism · Lifetime and RAII · Templates · C interoperability and robust design |

Every example lives in `app/src/main/assets/curriculum/{c,cpp}/NN-name.{c,cpp}` and ends with a
`/* expect: … */` block. `tools/verify_curriculum_examples.py` compiles each file with
`-Wall -Wextra -Werror` (`-std=c17` / `-std=c++17`), runs it with its optional `.stdin` file and
compares the real output. A lesson is only allowed to reference a file that this script compiles.

### Diagrams — `ConceptArt`

Original Canvas compositions, not stock images and not decoration:

| Art id | Explains |
| --- | --- |
| `lineage`, `cpp-lineage` | BCPL → B → C → C++, and C → C with Classes → C++ |
| `pipeline`, `files`, `debug` | compile/link stages, checked I/O steps, specification → test → diagnosis |
| `memory`, `init`, `reference`, `pointer` | storage, initialisation, aliases, address vs value |
| `io`, `streams` | input → program → output |
| `branch`, `loop` | selection paths and loop state/trace |
| `stack` | call frames and unwinding |
| `array`, `vector`, `struct` | contiguous indexing, bounds, members |
| `class`, `inherit`, `template` | encapsulation, runtime substitution, instantiation |
| `algorithms`, `raii` | range order and acquire/use/release lifetime |

Each has a caption that states the idea in words for accessibility.

## 4. Verification performed

- `./gradlew :app:testDebugUnitTest` — 72 tests, 0 failures (includes `LessonScriptTest`).
- `./gradlew :app:lintDebug` — clean.
- `./gradlew :app:assembleDebug` — debug APK produced.
- `python3 tools/verify_curriculum_examples.py` — 23 examples compiled and executed, 0 failures.
- `python3 tools/verify_toolchain.py --apk app/build/outputs/apk/debug/app-debug.apk` — packaged
  clang, lld, sysroot and memfs match the reviewed bytes.

## 5. Known limits (not claimed as done)

- No Android device or emulator was available in this environment, so the new surfaces were not
  launched or visually diffed on hardware. The layout is inset-aware and scrollable, but real
  device screenshots, TalkBack passes and font-scaling checks remain outstanding.
- Google sign-in still depends on the Appwrite/Google redirect configuration; a provider rejection
  is reported as a provider error, never as a user cancellation.
- Certificate examination/issuance, ad consent configuration and the Cloudflare deployment are
  unchanged by this work.
