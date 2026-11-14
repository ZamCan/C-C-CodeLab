#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p verification
gradle_bin="${GRADLE_BIN:-./gradlew}"
extra=()
if [[ -n "${ANDROID_AAPT2_OVERRIDE:-}" ]]; then
  extra+=("-Pandroid.aapt2FromMavenOverride=$ANDROID_AAPT2_OVERRIDE")
fi
"$gradle_bin" "${extra[@]}" testDebugUnitTest lintDebug assembleDebug --console=plain "$@" 2>&1 | tee verification/android-build.log
sha256sum app/build/outputs/apk/debug/app-debug.apk | tee verification/apk-sha256.txt
