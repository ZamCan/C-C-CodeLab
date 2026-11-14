# Android compiler investigation — 2026-11-14

## Current result

**Blocked: no verified redistributable Android toolchain is integrated, and no ADB-connected device is available.** The phone hosting this Termux session can receive the APK through the Android installer, but host shell compilation is not app-process execution evidence. The APK has no Compile/Run success path or simulated terminal output.

The current app targets API 35 with API 26 minimum. The working shell is Termux/PRoot on ARM64; its installed GCC/Clang tools and absolute Termux paths are not redistributable app runtime components by default. The SDK currently provides Android platform/build tooling, not a reviewed packaged offline C/C++ compiler for this application.

## Platform constraint

[Android's security guidance](https://developer.android.com/privacy-and-security/security-best-practices) says apps targeting Android 10+ cannot execute binaries from their writable home directory. [The Android 10 behavior changes](https://developer.android.com/about/versions/10/behavior-changes-10#execute-permission) describe the W^X restriction. Consequently, writing a native output file and calling ProcessBuilder on it is not a validated modern Android solution. Lowering targetSdk or relying on Termux's separate runtime is not adopted as a hidden workaround.

## Next bounded experiment

1. Inventory an ARM64 Android Clang/LLVM redistribution, license notices, linker, headers and C++ runtime; record immutable source/version/hash and package size. Build toolchain APK components in reproducible CI. Evaluate a packaged interpreter/WebAssembly runner only as a clearly disclosed alternative with an explicit supported-language/library contract.
2. Prototype compiler launch from APK-installed native code and an execution design permitted by modern Android. Do not assume a shared-library loader or writable executable mapping solves the security problem without proving it on the target OS.
3. Put untrusted execution in an isolated UID/process or verified runtime with no network, no access to learning/exam storage, bounded stdin/output, timeout, cancellation, and measured resource limits. The app UID sandbox alone is not sufficient.
4. Verify hello C and C++ iostream/vector programs, multi-file linking, exact stderr/status, syntax errors, infinite loop, output flood, memory exhaustion, cancellation, and filesystem boundaries. Repeat in airplane mode on the same phone and at least one minimum-supported configuration.
5. Only after those checks, connect Compile/Run to the editor and report original diagnostics and actual stdout/stderr. Record device model, Android version, ABI, commands, elapsed times, and package checksums in DELIVERY_PLAN.md.

This document is an investigation and acceptance plan, not a completed compiler integration. The proposed alternatives need license, device-policy, performance, and isolation validation before selection.
