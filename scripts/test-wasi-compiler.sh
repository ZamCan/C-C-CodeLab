#!/usr/bin/env bash
set -euo pipefail
: "${WASI_SDK_PATH:?Set WASI_SDK_PATH to an installed wasi-sdk directory}"
: "${IWASM:?Set IWASM to a WAMR iwasm executable}"
CLANG="$WASI_SDK_PATH/bin/clang"
CLANGXX="$WASI_SDK_PATH/bin/clang++"
SYSROOT="$WASI_SDK_PATH/share/wasi-sysroot"
for f in "$CLANG" "$CLANGXX" "$IWASM"; do test -x "$f" || { echo "Missing executable: $f" >&2; exit 2; }; done
test -d "$SYSROOT" || { echo "Missing WASI sysroot" >&2; exit 2; }
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
cat > "$tmp/hello.c" <<'C'
#include <stdio.h>
int main(void) { printf("C_OK\n"); return 0; }
C
cat > "$tmp/hello.cpp" <<'CPP'
#include <iostream>
#include <vector>
int main() { std::vector<int> values{1,2,3}; std::cout << "CPP_OK:" << values.size() << "\n"; }
CPP
cat > "$tmp/input.c" <<'C'
#include <stdio.h>
int main(void) { int n; if (scanf("%d", &n)!=1) return 3; printf("VALUE:%d\n", n*2); return 0; }
C
"$CLANG" --target=wasm32-wasip1 --sysroot="$SYSROOT" -std=c17 "$tmp/hello.c" -o "$tmp/c.wasm"
"$CLANGXX" --target=wasm32-wasip1 --sysroot="$SYSROOT" -std=c++17 "$tmp/hello.cpp" -o "$tmp/cpp.wasm"
"$CLANG" --target=wasm32-wasip1 --sysroot="$SYSROOT" -std=c17 "$tmp/input.c" -o "$tmp/input.wasm"
timeout 15 "$IWASM" "$tmp/c.wasm" > "$tmp/c.out"
timeout 15 "$IWASM" "$tmp/cpp.wasm" > "$tmp/cpp.out"
printf '21\n' | timeout 15 "$IWASM" "$tmp/input.wasm" > "$tmp/input.out"
grep -qx C_OK "$tmp/c.out"
grep -qx CPP_OK:3 "$tmp/cpp.out"
grep -qx VALUE:42 "$tmp/input.out"
cat > "$tmp/bad.c" <<'C'
int main(void) { return missing_symbol; }
C
if "$CLANG" --target=wasm32-wasip1 --sysroot="$SYSROOT" -std=c17 "$tmp/bad.c" -o "$tmp/bad.wasm" 2>"$tmp/bad.err"; then
  echo "Invalid C compiled unexpectedly" >&2; exit 1
fi
grep -q "error:" "$tmp/bad.err"
echo "PASS: C17, C++17, stdin/stdout, and compiler diagnostics through WASI/WAMR"
