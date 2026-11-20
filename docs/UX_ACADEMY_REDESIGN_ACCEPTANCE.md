# CodeLab C & C++ — Android UX and Learning Redesign Acceptance Specification

Status: requirements approved by product owner; implementation and device verification pending.

## Release-blocking functionality
- Fix Google OAuth Error 400 redirect_uri_mismatch by recording the actual redirect_uri from the failing request, matching it exactly in the Google Web OAuth client's Authorized redirect URIs, and testing the Appwrite callback. Do not treat registered Android SHA-1 fingerprints as proof that redirect configuration is correct.
- Fix offline compiler startup: on-device test report returned `Offline compiler asset unavailable: memfs` in all six tests. Inspect packaged APK assets, decompression and WebView asset resolution, then test real C and C++ compilation. Never show `Ready` before successful engine initialization.
- Verify auth session persistence, logout, and account separation; validate compiler stdin/stdout/stderr, diagnostics, exit status, syntax errors, and timeout.

## First launch and home
- New installs default to dark theme; support Light and System theme preferences and persist selection.
- First launch: concise CodeLab introduction with Get Started, Sign in with Google, and Continue as Guest. Explain sign-in benefits without forcing login. Do not prominently display the word 'Offline' in the welcome/header branding.
- Google sign-in button: official recognizable multicolour Google 'G' treatment and accessible label; do not use a monochrome placeholder. Respect Google's branding guidelines.
- Replace generic book artwork with distinctive, premium CodeLab C & C++ visual identity; keep both languages recognizable.
- Home top-right overflow menu (three dots) includes Theme, Account, Learning Progress, Preferences, Help and About. Do not scatter settings into oversized home cards.
- Returning users enter the workspace directly, without repeated welcome screens.

## Compiler interaction
- Open directly into a blank, editable, untitled buffer. No file creation or Save requirement to compile and run.
- Editor: C/C++ syntax highlighting, automatic indentation, line numbers, matching braces/brackets, code-aware editing, undo/redo, selectable text, and accessible cut/copy/paste toolbar.
- Keep primary editor chrome compact. Place Run prominently beneath editor, with Files / Projects beside it. Avoid a long stack of form controls and secondary buttons.
- Running code opens a horizontally animated output panel to the right; provide a clear swipe or Back-to-editor affordance. Show stdout, stderr, diagnostics, exit code and optional stdin in the output workspace.
- Distinguish engine loading, ready, running, failed, and cancelled states. Never label unavailable compiler assets 'ready'.
- Files / Projects is an optional separate workspace supporting source-file creation, import, rename, delete, multiple C/C++ files, project linking/build, and opening a file into the editor. Preserve unsaved buffers safely.
- Handle mobile keyboard, orientation, scrolling, and smaller displays without hiding editor actions.

## Academy and content
- Lesson pages should resemble polished academic teaching slides: concise learning objective, carefully worded definition, highlighted terminology, compact explanation, illustrative example, runnable code, short exercise and key takeaway.
- Persistent, functioning Contents, Previous and Next navigation; preserve lesson position; avoid ambiguous 'Continue' for page navigation.
- Tappable code illustration expands for reading and can open code in the compiler; syntax-colour examples with clear comments and optional output preview.
- Avoid dense paragraphs and excessive scrolling; split content into readable pages while preserving academic accuracy.
- Cover algorithms and problem-solving (plain language, pseudocode, flowcharts, decision tables), programming-language definitions, C history including Dennis Ritchie and Bell Labs in the early 1970s, C++ history and Bjarne Stroustrup, C vs C++ comparisons, types, variables, operators, I/O, conditionals, loops, arrays, functions, pointers, strings, structures/classes, and appropriate advanced topics.
- Translate mathematical examples into code step by step: Celsius to Fahrenheit, compound bank interest, grades, and progressively more challenging problems.
- Provide numerous original lab exercises and projects (calculator, login simulation, ATM simulation, library, grades, and additional staged projects), with starter, guided, and independent versions, expected outputs, and beginner-friendly feedback.
- Attribute sources and respect copyright; do not copy third-party lessons or artwork without permission.

## Acceptance evidence
- Screenshot and accessibility review of both themes on a small Android phone.
- Record device compiler tests, OAuth login/logout tests, navigation tests, editor keyboard/selection tests, and project multi-file tests.
- Treat this document as acceptance criteria, not evidence that implementation is complete.
