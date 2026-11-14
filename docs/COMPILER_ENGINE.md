# Android C/C++ compiler and Code Studio implementation contract

## Goal
Real **offline** C and C++ source compilation, linking and execution on Android; meaningful stdout/stderr/exit codes. A fake evaluator, canned output, web compiler, JavaScript simulation or Linux-host-only test does not satisfy this requirement.

## Engineering research before integration
1. Inventory repository Android sources, ABI targets, minSdk, current build system and licenses.
2. Evaluate legal and practical redistribution of a suitable Android-targeting compiler toolchain (Clang/LLVM or other validated solution), standard headers/libraries, linker and runtime. Verify package size and ABI support on target devices. A desktop compiler binary will not run on Android merely because copied into assets.
3. Determine whether Android app sandbox, SELinux, targetSdk restrictions and execution-from-writable-directory rules permit the proposed compiler/executable launch. Do not assume generated native executables can run on modern Android. Prototype on the target device first.
4. If direct on-device native execution is restricted, design an honest alternative (e.g., compatible sandboxed interpreter/WebAssembly execution pipeline for supported C/C++ subset) with verified toolchain and clearly documented limitations. Do not label a subset emulator as full native C/C++.
5. Measure memory, disk, startup and compile time on low-resource phones; prioritize ARM64 and document 32-bit compatibility.

## Suggested module interfaces
- `ProjectRepository`: create/list/open/update/delete/import/export; atomic writes; path traversal prevention.
- `EditorDocument`: encoding, file path, language mode, unsaved state, undo/redo, indentation and formatting.
- `CompileRequest`: project, translation units, standard (C11/C17 or C++17 baseline), compiler options, timeout.
- `CompileResult`: success, diagnostics, compiler stdout/stderr, exit status, artifact ID.
- `RunRequest`: artifact, stdin, timeout, max output, memory/resource budget.
- `RunResult`: stdout, stderr, exit status, timed out/terminated status, elapsed time.
- `TerminalSession`: streaming output and interactive stdin where supported.
- `ProjectTemplate`: manifest, files, expected test inputs/outputs and language requirements.

## Threat model
Treat all student source code as untrusted. Apply no network permission to runner, prevent reading app secrets or other projects, restrict file paths and subprocesses, limit CPU, memory, disk, stdout and duration, and ensure cancel/kill works. **Android app sandbox alone is not a safe boundary between native user code and the app's own private files.** A genuinely isolated runner may require a separate restricted process/runtime; document any unsolved risk. Never embed certificate signing keys or exam answers in the app.

## Editor requirements
Dark/light syntax coloring; accessible contrast; C/C++ tokenization; 4-space auto-indent; bracket matching; line numbers; monospaced font; line wrapping toggle; multi-file navigation; diagnostics with line/column, click-to-jump, copyable stderr; user-friendly compiler explanations without hiding original diagnostics.

## Verification matrix
- Valid C: hello world, arithmetic, loops, functions, multiple translation units, stdin, files where allowed.
- Valid C++: iostream, std::string, vector, classes, templates, multiple files, exceptions if supported.
- Invalid syntax: actual diagnostic message, line/column, nonzero status.
- Runtime: input, stdout/stderr separation, return codes, zero division handling, large output truncation, infinite loop timeout, memory exhaustion, cancellation.
- Persistence: restart app, restore unsaved project, import/export and project isolation.
- Real device: airplane mode, low memory, screen rotation/background, Android version and ABI compatibility.

Capture device model, Android version, ABI, toolchain version, exact commands, outputs and tests. A green host CI smoke test is only a prerequisite, not Android validation.
