# Offline compiler integration gate

The proposed architecture is **Android-hosted Clang/LLVM -> wasm32-wasip1 -> WAMR interpreter**.

This repository includes a reproducible **host-side** smoke test at `scripts/test-wasi-compiler.sh`. Supply an installed WASI SDK via `WASI_SDK_PATH` and an executable WAMR `iwasm` via `IWASM`. It verifies C17, C++17 (including `<vector>`), stdin, stdout, and real Clang diagnostics.

**This does not integrate Android-hosted Clang into the APK.** The test uses externally supplied binaries and must not be presented as on-device evidence.

## Required before shipping
1. Build an Android-hosted LLVM/Clang with WebAssembly target support for each intended Android ABI. Do not package desktop Linux host executables.
2. Bundle and verify a matching WASI sysroot, compiler resource directory, linker, and C++ libraries; record upstream licenses and hashes.
3. Embed a WAMR interpreter using the Android NDK and connect it through JNI to a separate worker process with no network or user-data access.
4. Enforce WASI capability restrictions, bounded memory, fuel/instruction limits or an independently killable process, capped output, bounded compilation time, and cancellation.
5. Replace the syntax-only `NativeClangEngine` with an implementation that emits actual wasm artifacts, checks exit codes and validates artifact tokens. Connect editor UI.
6. Run Android instrumented tests on API 26, 29, 35+ and ARM32/ARM64, including infinite loops, OOM, malformed wasm, C and C++ standard libraries, stdin and errors.
7. Publish a reproducible CI artifact and verify it on physical Android devices.

**Do not call the compiler complete until all seven gates are met.**
