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
