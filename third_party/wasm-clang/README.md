# Compiler sources

memfs.c and stb_sprintf.h are preserved from cppstudio-io/wasm-clang-runtime commit df1180d80184733c6a01599f76b92b4001d20f87. Apache 2.0 and stb MIT/public domain notices are in app/src/main/assets/compiler/licenses. The corresponding modified JS runtime is bundled as shared.js; changes are marked in its header. Compiler, linker and sysroot are release v0.1.0 artifacts, pinned by docs/compiler-artifacts.json.

Local memfs build: clang --target=wasm32-wasip1 --sysroot=SYSROOT -isystem SYSROOT/include/wasm32-wasip1 -O2 -c memfs.c -o memfs.o; compile stb_sprintf.h with the same arguments plus -DSTB_SPRINTF_IMPLEMENTATION -x c. Link using wasm-ld -LSYSROOT/lib/wasm32-wasip1 --no-entry --export-dynamic --allow-undefined --initial-memory=4194304 --max-memory=134217728 memfs.o stb_sprintf.o -lc -o memfs. SYSROOT is the pinned sysroot22.tar extracted with path/link validation. Rebuilt binaries need reviewed hash updates.
