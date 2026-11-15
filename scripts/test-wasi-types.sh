#!/usr/bin/env bash
# Run against an externally installed WASI SDK and WAMR; does not prove Android packaging.
set -euo pipefail
: "${WASI_SDK_PATH:?Set WASI_SDK_PATH}"
: "${IWASM:?Set IWASM to iwasm executable}"
cc="$WASI_SDK_PATH/bin/clang"
cxx="$WASI_SDK_PATH/bin/clang++"
sysroot="$WASI_SDK_PATH/share/wasi-sysroot"
for exe in "$cc" "$cxx" "$IWASM"; do test -x "$exe" || { echo "Missing $exe" >&2; exit 2; }; done
test -d "$sysroot"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
cat > "$work/types.c" <<'EOF'
#include <stdio.h>
#include <stdbool.h>
#include <stdint.h>
#include <string.h>
#include <math.h>
int main(void) {
    int n=12; float f=2.5f; double d=3.25; long double ld=4.0L;
    bool yes=true; char letter='A'; char name[]="CodeLab";
    int32_t signed32=-7; uint64_t unsigned64=42;
    double result=sqrt(144.0)+pow(2.0,3.0)+sin(0.0)+cos(0.0);
    if(n!=12 || f!=2.5f || d!=3.25 || ld!=4.0L || !yes || letter!='A'
       || strcmp(name,"CodeLab") || signed32!=-7 || unsigned64!=42 || result!=21.0) return 3;
    puts("C_TYPES_OK");
    return 0;
}
EOF
cat > "$work/types.cpp" <<'EOF'
#include <iostream>
#include <string>
#include <vector>
#include <array>
#include <map>
#include <cmath>
#include <cstdint>
int main() {
    int n=12; float f=2.5f; double d=3.25; long double ld=4.0L;
    bool yes=true; char letter='A'; std::string name="CodeLab";
    std::int32_t signed32=-7; std::uint64_t unsigned64=42;
    std::vector<int> v{1,2,3}; std::array<int,2> a{{4,5}};
    std::map<std::string,int> m{{"answer",42}};
    double result=std::sqrt(144.0)+std::pow(2.0,3.0)+std::sin(0.0)+std::cos(0.0);
    if(n!=12 || f!=2.5f || d!=3.25 || ld!=4.0L || !yes || letter!='A'
       || name!="CodeLab" || signed32!=-7 || unsigned64!=42 || result!=21.0
       || v.size()!=3 || a[1]!=5 || m.at("answer")!=42) return 3;
    std::cout << "CPP_TYPES_OK\n";
}
EOF
"$cc" --target=wasm32-wasip1 --sysroot="$sysroot" -std=c17 "$work/types.c" -lm -o "$work/types-c.wasm"
"$cxx" --target=wasm32-wasip1 --sysroot="$sysroot" -std=c++17 "$work/types.cpp" -o "$work/types-cpp.wasm"
timeout 20 "$IWASM" "$work/types-c.wasm" > "$work/c.out"
timeout 20 "$IWASM" "$work/types-cpp.wasm" > "$work/cpp.out"
grep -qx C_TYPES_OK "$work/c.out"
grep -qx CPP_TYPES_OK "$work/cpp.out"
echo "PASS: C17 and C++17 numeric, bool, string, fixed-width, math and collections"
