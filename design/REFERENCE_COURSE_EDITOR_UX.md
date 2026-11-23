# CodeLab — Reference-driven Android UX specification

**Purpose:** This branch is a design handoff for engineering agents. It does not replace `main` or claim any feature is implemented. Reference screenshots were provided in the project conversation on 23 Nov 2026; the original screenshots are not committed here. They are references, **not** permission to copy third-party branding, source code, icons, or artwork.

## Reference observations

### Course discovery
The screenshots show a dark course catalog with a compact top bar, search, horizontal category filters, a distinct **In your library** area, a horizontally browsable **Popular** section, and a vertically browsable **All** section. Entries communicate course title, lesson count, approximate time, offline availability, and progress/download information. Visual hierarchy is strong, with short labels, restrained icons, and clear grouping.

**CodeLab adaptation:** Restrict to the C and C++ academy; provide Fundamentals, C, C++, Algorithms, Practice, Projects, Exams, and Saved. Use factual lesson counts from bundled curriculum, not placeholder statistics or fabricated download numbers. Include downloaded/offline state only if the implementation actually tracks it. Avoid dense cards and oversized promotional hero areas.

### Editor / compiler
Screenshots show a dark editor with monospaced syntax-colored C++ code, line numbers, current-line highlighting, a visible Run triangle, a top-right overflow menu, and a narrow symbols/navigation row immediately above the Android keyboard. Menu examples: Input, New, Open, Save, Save As, Clear, Reference, Settings. One reference app displays **Error opening input file**; this is a failure case to avoid, not an expected result.

**CodeLab adaptation:** Real, functional offline C/C++ compilation and execution, not simulated output. Start with an immediately editable scratch buffer; no file-save gate. Provide language selector, Run/Stop, code completion suggestions, syntax colors, autoindent, bracket pairs, undo/redo, tab, cursor navigation, and symbol shortcuts. Put stdout, stderr, exit status and compiler diagnostics in a separate accessible output panel; keep the keyboard from obscuring editing controls. Multi-file projects and stdin should work as implemented, not as inert menu entries. Errors must identify the cause and recovery action. The previously observed `Offline compiler asset unavailable: memfs` is a blocker; verify that every required asset is packaged and can be loaded on a real device.

### Branding and splash
Reference shows a full-bleed blue-violet gradient with a large white C++ mark and restrained pixel/block motifs. **Do not duplicate the mark or exact composition.** Build a distinct CodeLab C & C++ logo, wordmark, and brief native-feeling splash; support dark and light appearance consistently. Avoid a second unnecessary welcome screen or a giant decorative card.

## Teaching experience: a professor-led, phase-based lesson

**Non-negotiable:** A Next button advances to the **next beat in the current narrative**, not to a new unrelated topic. Present a focused amount of text per beat. Back restores the previous beat. A chapter index permits deliberate jumps, but mandatory brainstorming/checkpoint activities cannot be bypassed when credit is sought.

1. **Hook:** One surprising, accurate question or prediction.
2. **Meet the guide:** Illustrated Dennis Ritchie introduces C in an explicitly fictionalized, playful dialogue. Later illustrated Bjarne Stroustrup introduces C++ and explains its relationship to C. Attribute actual historical facts accurately. Clearly mark imagined dialogue; do not present invented quotations as authentic.
3. **Explain:** One idea, one example, one visual.
4. **Trace:** Pointing diagram, memory/state flow, decision table, or code execution timeline; highlight the exact line under discussion.
5. **Predict:** Learner chooses an outcome before code runs; reveal explanation after response.
6. **Try:** Editable working example in the real compiler, with optional hints and diagnostics.
7. **Brainstorm:** A required, accessible short response, matching, ordering, or reasoning checkpoint.
8. **Reflect:** Recap, practical connection, and the next concept.

Sample **fictional dialogue** (not historical quotation):
- Dennis: “I helped create C at Bell Labs. A small set of tools can solve remarkably large problems. Let's see what a variable really stores.”
- Bjarne: “I built C++ starting from C, adding features for larger programs. Dennis gave me a strong foundation—but now I'll challenge you to design something bigger.”
Use humor without false technical claims (C++ was not merely 'Simula renamed'; Simula influenced its object-oriented features).

## Visual system

- Native Android layout; responsive on narrow screens and with enlarged system fonts.
- Dark-first palette with excellent contrast, coherent light theme, 8-point spacing rhythm, limited accent colors, stable navigation and consistent button geometry.
- One primary action per screen; avoid duplicate navigation bars, excessive nested cards, and long scrolling paragraphs.
- Lesson diagram style: clean labeled arrows, stepwise state changes, callout pointers, optional illustrated historical figures, accessible alt descriptions.
- No copyrighted screenshots or artwork bundled without permission. Create original illustrations or use properly licensed assets with attribution.
- Provide content in Swahili and English when translations are actually available; avoid machine-like filler.
- Preserve offline-first reading, editing, and progress; account sign-in remains optional.

## Engineering acceptance tests

- [ ] On a real Android device, compile and run C and C++ Hello World offline with network disabled.
- [ ] stdin, stdout, stderr, nonzero exit code, syntax error, and infinite-loop timeout behave correctly.
- [ ] No missing `memfs`, sysroot, clang, linker, or worker assets; verify APK contents and runtime loading.
- [ ] Google OAuth redirect URI matches the Appwrite/Google Cloud configuration; error 400 is resolved before calling sign-in complete.
- [ ] Editor retains cursor and scratch content on keyboard open/close, orientation change, and navigation.
- [ ] Keyboard shortcut row remains usable; selection, paste, indentation, and undo work.
- [ ] Lessons advance beat-by-beat, support Back, and do not skip mandatory reasoning checkpoints.
- [ ] History dialogue is explicitly fictionalized; historical and programming claims are checked.
- [ ] Course counts, progress, offline labels, and completion status are computed from real data.
- [ ] Dark/light theme, screen reader labels, font scaling, contrast, and back navigation pass manual checks.
- [ ] No copied third-party branding or proprietary artwork.

## Agent execution order

1. Fix offline compiler packaging/runtime failure and instrument device diagnostics.
2. Establish unified native design tokens, navigation and app identity.
3. Rebuild editor interactions and accessible output surface.
4. Build the staged lesson player and visual/interactive checkpoint engine.
5. Populate and verify full C/C++ curriculum and practical exercises.
6. Rebuild course discovery from real curriculum metadata.
7. Validate on-device and CI; only then address production OAuth, certificate issuance and ads.

**Merge policy:** Keep this reference branch separate for review. Implement code on `main` only after tests and review. Do not claim this design handoff itself fixes the app.
