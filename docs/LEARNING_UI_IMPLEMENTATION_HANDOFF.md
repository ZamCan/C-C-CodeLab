# CodeLab — Native Learning Experience Implementation Brief

Status: DESIGN HANDOFF / NOT A CLAIM OF IMPLEMENTATION
Reference: user-supplied Android screenshots reviewed 2026-11-15.
Target: `preview/android-foundation`. This document is an instruction to implement and verify, not a screenshot or a copied third-party UI.

## Non-negotiable experience

- **Native Android, not a website.** White (#FFFFFF) is the dominant canvas. Dark graphite (#202536) for reading text, subdued grey (#596277) for secondary copy. Use blue (#425FF5) to violet (#7851DB) selectively for progress and the primary bottom action. Reserve coral/teal/gold for meaningful illustrations, feedback and milestones. Do not fill every page or panel with a gradient.
- **One screen = one instructional beat.** Show a concise definition, explanation, illustration, worked example, or question, then a user-controlled Next action. Never concatenate a chapter or multiple questions into one scrolling page. Lesson and quiz screens must have no vertical ScrollView. A course catalogue can scroll.
- **Content must fit the device.** Calculate available height after status/navigation insets, header, progress, body, and bottom action. Split paragraphs at semantic boundaries and paginate code examples by logical blocks. Never clip or shrink text below readable size to satisfy no-scroll. Test at 320dp width, 360dp width, 400dp width, large font (1.3x), display scaling, and landscape. On compact devices, reflow or create another page.
- **Progress at the top.** Thin rounded segmented indicator with completed/current segments colored and remaining segments pale. Progress is based on actual pages and assessments, not decorative fake percentages.
- **Navigation.** Close/back at upper left; bottom anchored large rounded Next / Check Answer button within safe area. Optional Previous must not compete with primary action. Close confirms when unsaved work would be lost. Do not place a persistent tab bar inside a full-screen lesson.
- **Readable teaching.** Clear title, comfortable line spacing, intentional whitespace, short paragraphs, selective diagrams or original illustrations, readable code blocks. Keep source code in monospaced, syntax-highlighted, selectable text; explain syntax tokens and why they appear in that location.
- **Course overview.** Show two separate C and C++ pathways, expandable modules with completed/total lesson counts and a clear resume affordance. Algorithmic foundations are optional/skippable; C/C++ language introduction is required before formal pathways. Use modest illustrated accents, not a giant card per paragraph.
- **Offline-first.** All learning pages, quizzes, progress, and explanatory feedback work without a network. No loading/download blocking screen for bundled lessons.

## Screen-by-screen contract

1. **Course map:** white canvas, compact app header, clean module list, module completion counts, one clear Continue action. Catalogue scrolling permitted.
2. **Reading/story page:** segmented progress, close, topic heading, at most one instructional beat, optional original educational illustration, anchored Next. No vertical scrolling. The same topic may require several pages.
3. **Worked example:** explain input -> processing -> output, show source with legible formatting, then allow a separate step for line-by-line or token-by-token explanation. Provide 'Open in Studio' as an explicit action where practical.
4. **Checkpoint introduction:** distinct transition page stating what the quiz covers and that it is an *offline practice check*, not a certificate exam.
5. **Question:** one question at a time; 2–5 large selectable answer rows as appropriate; clear selected state; Check Answer disabled until a choice is made. No next question before feedback. Include reorder questions only with functional accessible controls, not decorative drag handles.
6. **Correct response:** positive but restrained visual feedback; show the correct reasoning, not only 'Correct'. Continue advances exactly once; persist completion.
7. **Incorrect response:** supportive correction with the reason and an explicit retry/review action. Do not auto-advance or mark complete.
8. **Checkpoint summary:** show genuine score (correct/attempted), accuracy, and elapsed time measured by a monotonic clock, plus next topic. No fabricated 100%, time or certification.
9. **Completion:** distinguish offline lesson progress from optional future verified online certificate issuance. Do not show a certificate as earned until verified issuance actually exists.

## Teaching and academic integrity

- Optional preface: algorithms -> algorithms/computers -> representation methods (plain language, flowchart, pseudocode, decision table, and actual programming language; acknowledge that classifications vary) -> C/C++ programming-language introduction -> language histories -> independent C and C++ paths.
- Keep all foundation topics skippable; C/C++ introductory material is not skippable in the normal first-run pathway.
- Explain C history (Dennis Ritchie, Bell Labs, early 1970s), and C++ (Bjarne Stroustrup, 'C with Classes' from 1979, C++ name in 1983) with precise wording. Avoid ambiguous 'C++ was developed in YEAR' multiple-choice questions that lack an agreed milestone.
- Explain source -> preprocessing/compilation -> object code -> linking -> executable -> runtime at an appropriate beginner level. Do not claim that the computer directly executes the source text, or that a compiler always directly produces a single binary.
- Every quiz must have an explicit answer key, reasoning, and a content review. No fabricated compiler output or exam credentials.

## Implementation worklist

- [ ] Extract a reusable native `LearningStoryActivity` or screen controller with stable state and semantic page models (definition, paragraph, diagram, source, challenge, quiz, feedback, summary).
- [ ] Add a native theme and typography resources; remove dark Aurora backgrounds and glass cards from learning screens.
- [ ] Separate course-map layout from full-screen lesson layout; avoid nested scroll views.
- [ ] Paginate from authored semantic lesson blocks, **not arbitrary 350-character cuts**. Keep formulas, code fences, list items and examples together.
- [ ] Persist current course/module/page, quiz selections where appropriate, completion, and review state; restore after process death and rotation.
- [ ] Implement selectable answers with accessible labels and correct/incorrect feedback, then Next.
- [ ] Add real question counts, timing, scoring and a completion summary.
- [ ] Test no clipping and correct safe-area behavior across device sizes and font scaling.
- [ ] Run Gradle unit tests, lint and debug APK build; inspect APK on a physical phone before marking complete.
- [ ] Add screenshots of **our own** built UI to a visual QA report and compare against this brief.
- [ ] Preserve existing Studio/source files, offline data, and progress migration; do not break working capabilities during redesign.

## Acceptance criteria / release gate

A reviewer can open a lesson and proceed through multiple pages without scrolling; no page is clipped at large font scale; one quiz question is visible at a time; a selected answer is obvious; Check Answer shows reasoning; correct answer unlocks the next step; incorrect answer offers retry; a completed checkpoint reports real metrics; back/resume works; the course map remains usable; all of this works offline. Build and visual evidence are required. Until then mark this **IN PROGRESS**, not complete.

## Temporary handoff policy

This brief may be deleted **only after** all acceptance criteria pass and its requirements have been transferred into permanent product documentation, tests, and verified native UI. Do not delete it merely because a screen looks similar. Do not commit or redistribute the third-party reference screenshots; use original graphics and implementation.


## Additional screenshot study — second reference batch (2026-11-15)

The supplied screenshots show a **progressive reveal** sequence: (a) a question introducing how computers understand instructions; (b) a centered topic heading with a modest programmer illustration; (c) an explanation revealed after the initial question; (d) an applications topic with individual bullets appearing across subsequent steps; and (e) a friendly transition to the next course section. They also show a header with a close icon, segmented progress, a share affordance, and a bottom action that alternates between a prominent gradient Next button and subtle "Tap to Continue" text. The screenshots are reference observations, not licensed assets.

### Additional exact interaction requirements

- Model each lesson as ordered **beats** with optional incremental **reveals**. A beat can contain a question hook, definition, image, explanatory paragraph, bullet, or conclusion. A tap should advance a reveal *within the same beat* where appropriate; after the final reveal, it advances to the next beat. Do not confuse revealed bullets with completed lessons.
- **Progress segments** represent actual authored steps, with progress advancing consistently. A reveal may leave the segment unchanged until the beat completes. Restore reveal position on app resume.
- For content with 3–7 bullets, display the heading and first item, then reveal additional items one at a time. Preserve earlier items only while the layout has enough space; otherwise split to a new page. No overflowing or hidden bullets.
- **Conversational hooks** can ask a learner to think before the answer appears. Use concise, grammatically polished academic English rather than copying informal or erroneous wording. Example: "How does a computer execute the instructions we write in C++?" followed by "Let's trace the journey from source code to a running program."
- **Illustration placement:** optional small, original programmer/terminal/CPU/compiler visual centered below the heading; never force a decorative image when a diagram or code is more instructive. Illustrations must be original or appropriately licensed.
- **Interaction clarity:** choose one primary progression method for the product. Prefer a bottom Next button; if a full-page tap-to-continue mode is offered, it must be opt-in/clearly signposted, work with accessibility services, and avoid accidental advancement while selecting text or code. Do not randomly alternate controls between screens.
- **Share and expand:** only show Share when it actually shares a lesson deep link or permitted original content. Only show Expand when it opens a functional, accessible larger diagram. Do not display inert icons merely to match screenshots.
- **End-of-section transition:** show a short recap and clear next module; avoid excessive praise or a false sense of certification. A "Continue to C++ Fundamentals" action is better than a vague Next when switching sections.

### Content corrections to apply when translating the reference pattern

- Define a programming language as a formally specified notation for expressing computations and instructions. Explain that C/C++ source code is processed by toolchains and executed via compiled programs; the CPU does not directly interpret ordinary C++ text.
- Explain preprocessing, compilation, linking, loading, and execution as a staged conceptual model; note that toolchain details vary.
- Applications of C++ may include game engines, real-time systems, browsers, database engines, financial software, and language runtimes. Do not assert that an entire company, website, ATM, JVM, or browser is implemented in C++ merely because some components may be. Verify named company/product claims before publishing them.
- Avoid ambiguous or unsupported examples such as "C++ is used in ATM machines" without a specific documented system.
- Make the C and C++ pathways distinct: the introductory source-to-execution story can be shared, but examples must use the correct language and standard libraries.

### Second-batch acceptance scenarios

- [ ] The introductory question is followed by its explanation in a deliberate authored sequence.
- [ ] A small original illustration can appear on a lesson beat without forcing scroll or shrinking body text.
- [ ] Multi-item application lists reveal in stages, preserve legibility, and do not clip on compact screens.
- [ ] Progress, page and reveal state restore after rotation and process recreation.
- [ ] All displayed Share/Expand/Next actions are functional, and the primary advance control is consistent.
- [ ] Academic reviewers confirm that examples and source-to-execution explanations are precise and not copied uncritically.


## Third screenshot batch — welcome, mentor framing, formative quiz, feedback (2026-11-15)

Reference screenshots show a nine-segment introductory lesson with an illustrated welcome, a "small tip" that reveals a second and third paragraph on subsequent taps, an illustrated "Meet C++" history beat that reveals information incrementally, and a single-select four-option question ("C++ was developed as an extension for?"). The Check Result action is initially disabled and becomes enabled after selecting an answer; selection uses a prominent filled state. A separate correct-answer presentation appears after checking. These observations are **UI references**, not permission to copy artwork, dialogue, or branding.

### Required CodeLab learning model
1. **Welcome beat:** a short original welcome and clear expectations; no fabricated historical figure speaking in first person or promise of guaranteed mastery.
2. **Study tip beat:** teach a concrete learning strategy: read the explanation, predict what code will do, run it, inspect errors, and modify it. Provide an actionable **Open in Code Studio** control where the lesson has runnable code.
3. **Meet C / Meet C++ beats:** original visual treatment, accurate historical attribution in third person. Dennis Ritchie developed C at Bell Labs in the early 1970s. Bjarne Stroustrup began work on C with Classes at Bell Labs in 1979; the name C++ was adopted in 1983. Present these as historical background, not a fabricated personal speech.
4. **Formative quiz beat:** radio-style options with single selection, disabled Check Answer until selection, visibly selected state, keyboard/screen-reader accessibility, no automatic submit on selection, and explanation-based feedback for both correct and incorrect choices.
5. **Result beat:** show whether the answer was correct, what the correct answer is, and **why**; support Review Explanation and Continue. No misleading celebratory screen that omits the explanation.
6. **Learning record:** save completed beats and quiz attempts locally; distinguish practice feedback from scored certificate exams. A wrong answer should not block course access.
7. **Layout:** bottom primary action stays above system navigation and IME; adaptive insets and scrolling protect content on compact devices; no excessive blank vertical space when the lesson can use it meaningfully.
8. **Brand:** original CodeLab blue/violet identity, accessible light/dark contrast and modest illustration sizes; no copying of the reference app's mascot or imagery.

### Additional acceptance tests
- [ ] The quiz's Check Answer is disabled until an option is selected and enabled immediately afterward.
- [ ] Selecting another option replaces the previous selection, without prematurely grading.
- [ ] Correct and incorrect feedback both give an accurate reason and a route to revisit the concept.
- [ ] Practice attempts persist locally and remain separate from secure online certification attempts.
- [ ] Code-along buttons load a real editable source example; run is not advertised as functional until an actual compiler is integrated and tested.
- [ ] Historical introduction uses third-person attribution and no impersonation or invented endorsement.
