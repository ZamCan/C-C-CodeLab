# ZamCan Academy — complete product, visual and pedagogy specification

## 1. Product identity and navigation
Continue CodeLab C & C++ as **ZamCan Academy**, a broader multi-subject offline-first educational Android app. Boot should be fast: polished ZamCan Academy wordmark, subtle animation, no unnecessary welcome slides. Two equal prominent home destinations:
- **Code Studio**: free unlimited C/C++ editor, terminal, projects, samples, local compilation and execution.
- **Learning Academy**: free courses, progress, notes, exercises, flash questions, quizzes, and post-course projects.
Profile, downloads, achievements, certificates, course preferences and settings are secondary navigation, not competing primary home cards.

## 2. Visual direction
Premium **native Android** appearance rather than a website-like stack of oversized cards. Consistent design system across boot, editor, lesson, quiz, exam, certificate and settings. Sophisticated dark navy/graphite base with restrained blue-violet highlights; equally polished light theme, accessible contrast, generous spacing, subtle motion and responsive layouts. Use proper vector icons and original assets; do not fabricate a previously approved logo. Review existing assets first and preserve the best existing logo. Typography hierarchy: readable body text, compact headings, monospaced code; avoid loud gradients everywhere.

### Code editor design
- Syntax color categories: preprocessor directives, headers, types, keywords, identifiers, strings/chars, numbers, comments, operators, errors, selected text. Support high-contrast dark and light palettes.
- Four-space indentation by default; Tab inserts spaces, Enter carries indentation, auto-indent after opening brace, dedent on closing brace, configurable tabs/line wrap/font size, bracket matching, optional autocomplete and accessible cursor/selection.
- Show source line numbers, file tabs, project tree, language mode C/C++, compile and run controls, stdin input, stdout/stderr/diagnostics panel, compiler exit status and error line navigation.
- Explain code indentation with before/after comparisons and scope visualization; never suggest indentation alone changes C/C++ semantics.

## 3. Course and lesson experience
Use short navigable lesson stages inspired by the user's example of progress segments, tap-to-reveal explanations, immediate quiz feedback, and completion celebrations, but do not copy another application's UI or wording. Lessons should be **substantially deeper** than brief flashcards. Navigation: topic map → concept → guided explanation → annotated code → editable experiment → checkpoint → recap. Offer pause/resume and offline bookmarks. Include a clear learning path, not an endless vertical wall of text.

For each code example, explain every syntactic component in context: exact token, lexical category, purpose, where allowed, language-specific rules, failure cases and experiment. Example: `#include <stdio.h>` vs `#include <iostream>`; `int main(void)` in C vs `int main()` in C++; `printf` vs `std::cout`; `using namespace std;` risks; semicolon placement; newline `\n`; compile/link/run distinctions; why `return 0;` is optional at end of main in modern C/C++.

### Proposed course modules
**Shared foundation:** What a program is; algorithm, pseudocode, flowchart, decision table; C/C++ history and use cases; source files, toolchains, compilation and linking; code style; errors and debugging.
**C track:** headers, main, input/output, types, expressions, branching, loops, arrays/strings, functions, scope, pointers, structures, memory, files, modular compilation, testing.
**C++ track:** C++ I/O, types/references, strings/vectors, functions/overloading, RAII, classes/objects, constructors, encapsulation, inheritance and polymorphism where appropriate, templates/STL, exceptions, file I/O, build organization and testing.
**Comparative lessons:** C vs C++ headers, compilation modes, standard libraries, compatibility limits, `extern "C"` linkage, multi-file projects.
**Math/algorithm problems:** Compound interest starting at 1000, 10% annually until >2000; Celsius→Fahrenheit `F = (9.0/5.0) * C + 32`; number-system conversions, grades, searches, and realistic input validation.
**End-of-course independence:** How to plan, create, compile, debug, test, and document a full C or C++ program, including multi-file code; explain which outputs the app can genuinely run and which require external platforms.

Content is versioned, locally packaged or downloadable for offline use, searchable, and tested for correctness. Reference authoritative texts without reproducing large copyrighted passages. Include concise citations/reading recommendations and mark original teaching material.

## 4. Offline learning engines
- Structured courses and prerequisites; progress per learner, spaced repetition, flash questions, self-tests, guided exercises, explanations for incorrect answers.
- Quiz variants: true/false, multiple choice, fill in code, trace execution, fix error, output prediction, code ordering; feedback should teach rather than merely score.
- Learning progress stored locally using Room/SQLite, with schema migrations and backup/export. Offline progress cannot by itself authorize trusted certificate issuance.
- No internet, registration, subscription or ads required for the core learning experience.
- Accessibility: TalkBack semantics, dynamic font scaling, keyboard navigation where possible, color-independent correctness indicators, reduced motion.

## 5. Code Studio and project workspace
Multiple projects and source files; create/open/rename/copy/delete/export/import; templates; safe file handling and persistence. Offer single-file and multi-file C/C++ projects, compile flags visible in an advanced panel, stdin support, terminal output and diagnostics. User may write original problems/programs. The editor is free and unlimited subject only to device resource/safety constraints.

## 6. Five post-course guided projects
Unlock after completion of the substantial course path, independently of the optional certificate exam. For each project provide requirements, algorithm, flowchart/pseudocode, annotated C and/or C++ solution, starter code, editable project, milestones, tests, and expected results.
1. **Calculator:** arithmetic, functions, validation, optional scientific operations.
2. **Educational login system:** register/login/logout, records and validation; clearly distinguish demonstration from production authentication. Do not store plaintext passwords.
3. **Student results:** add/search students, marks, averages, grades, local files.
4. **ATM simulator:** fictional accounts, deposit/withdraw/balance, transaction rules; no real banking integration.
5. **Library system:** add/search books, borrow/return, status, local persistence and optional overdue calculations.

### Static internal emulator vs real output
A prepared visual UI can demonstrate how a calculator, login screen or ATM *might* look. Label it **Static Project Preview**. A separate **Live Console** shows the actual stdout/stderr/exit status from compiled C/C++ source. Do not pretend that standard console C/C++ code generated the preview. Optional safe adapters may map a narrow, documented program output protocol to widgets, but only when actually implemented and tested. No unsupported claims of APK creation or unrestricted GUI development.

## 7. Broad academy future
Learning, assessment, local content and credentials are subject-agnostic. Later courses (mathematics, science, languages, computer studies) register their own modules and grading strategies while sharing progress, accessibility, offline sync and credential interfaces. C/C++ quality and reliable compiler are the first release priority.

## 8. Monetization and trust
Core learning and coding free; certificates free at launch. Ads optional, restrained and nonblocking; never show an interstitial in the timed exam or compiler terminal. No fake accreditation or third-party partner claims. The academy issues its own educational achievement certificates.
