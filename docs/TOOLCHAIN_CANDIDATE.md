# Offline compiler candidate — research, not shipped integration

The 0.2.0 preview does **not** bundle a compiler. No additional library is claimed
as supported by the Android application until its compile/link/run checks pass
inside that application on a physical device.

## Exact candidate

- Source: https://github.com/binji/wasm-clang
- Commit: `648c4a89997a351eef75cdaec3ef5b89d4937dec`
- Clang/LLD WebAssembly distribution with Clang 8.0.1 resource directory.
- Target: `wasm32-wasi`, not Android ARM64 native binaries.
- Assets: `clang`, `lld`, `memfs`, `sysroot.tar`, `shared.js`.
- Exact artifact sizes and SHA-256 values are recorded by
  `verification/wasm-candidate-probe.log`.
- Upstream describes this as an alpha demonstration. Its age and incomplete
  runtime make it a research candidate, not an approved production dependency.

## Libraries actually found

The sysroot contains C headers, libc, C++ headers, libc++, libc++abi, `crt1.o`,
and `lib/clang/8.0.1/lib/wasi/libclang_rt.builtins-wasm32.a`.
The builtins archive is needed for the tested C++ algorithm link; omitting it
produced an actual undefined `__lttf2` symbol from libc++.

`libm.a`, `libpthread.a`, `libdl.a`, `librt.a`, `libresolv.a`, `libutil.a`,
`libcrypt.a`, `libxnet.a`, and `libcanvas.a` are eight-byte archives in this
distribution. They must not be advertised as working implementations of those
platform capabilities. The tested square-root function is supplied through libc.

No Boost, OpenSSL, SQLite, SDL, Qt, OpenCV, Android NDK/JNI, operating-system
process API, networking, or threading support is established. Third-party
libraries require a compatible target, reviewed version/license, dependencies,
size/performance review, and actual feature tests. There is no meaningful finite
package containing every C/C++ library.

## Reproducible experiment

Clone the source, check out the exact commit above, then run:

```sh
node tools/probe_wasm_toolchain.cjs /path/to/wasm-clang
```

The probe compiles real C11/C++17 source with Clang, links objects with LLD,
and executes the resulting WebAssembly. It uses only the candidate's virtual
filesystem. Separate workers provide a supervisor timeout for the infinite-loop
test. It does not mount application projects or host files into the guest.
This Node-host experiment does not establish Android WebView security or speed.

## Gates before APK integration

1. Review complete LLVM, libc++, WASI-libc/musl, and runtime notices and origins;
   the upstream repository license alone is not the full redistribution review.
2. Choose a maintained, reproducibly built distribution; pin artifact checksums
   and reject mismatches before executing compiler/runtime code.
3. Replace demo glue: separate stdout/stderr, correct UTF-8 and exit statuses,
   import allowlist, fresh per-run filesystem, bounded memory/output/disk,
   cancellation, and independently killable compiler and program workers.
4. Package all assets offline with restricted WebView asset access and no
   unrestricted JavaScript/native bridge. Validate renderer process isolation.
5. Run the COMPILER_ENGINE matrix on the installed Android app, including
   airplane mode, multi-file C/C++, exceptions capability, memory exhaustion,
   output floods, cancellation, project-boundary checks and lifecycle behavior.
6. Only then expose Compile/Run with an explicit WebAssembly capability label.
   Full native Android compilation remains a separate unfulfilled requirement.
