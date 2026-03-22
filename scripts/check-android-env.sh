#!/usr/bin/env sh

set -eu

SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-/usr/lib/android-sdk}}"
JAVA_HOME_VALUE="${JAVA_HOME:-"(not set)"}"

echo "Android SDK root: $SDK_ROOT"
echo "JAVA_HOME: $JAVA_HOME_VALUE"

check_dir() {
  if [ -d "$1" ]; then
    echo "[OK] $1"
  else
    echo "[MISSING] $1"
  fi
}

check_dir "$SDK_ROOT"
check_dir "$SDK_ROOT/platform-tools"
check_dir "$SDK_ROOT/platforms/android-34"
check_dir "$SDK_ROOT/build-tools/34.0.0"
check_dir "$SDK_ROOT/licenses"

if [ -d "$SDK_ROOT/platforms/android-34" ] && [ -d "$SDK_ROOT/build-tools/34.0.0" ]; then
  echo "Android SDK looks sufficient for this project's current compileSdk/buildToolsVersion."
else
  echo "Android SDK is incomplete for this project."
  cat <<'EOF'

Debian/Ubuntu package hint:
  sudo apt-get update
  sudo apt-get install -y \
    google-android-licenses \
    google-android-cmdline-tools-13.0-installer \
    google-android-platform-34-installer \
    google-android-build-tools-34.0.0-installer
EOF
  exit 1
fi
