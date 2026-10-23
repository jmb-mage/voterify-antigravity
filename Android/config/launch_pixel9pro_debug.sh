#!/usr/bin/env bash
#
# launch_pixel9pro_debug.sh
# Provisions the Google Pixel 9 Pro (API 35 / Android 15) AVD if not already created,
# boots the emulator, builds the debug APK, and launches Voterify with debugger support.
#

set -euo pipefail

AVD_NAME="Pixel_9_Pro_API_35"
DEVICE_PROFILE="pixel_9_pro"
SYS_IMAGE="system-images;android-35;google_apis_playstore;arm64-v8a"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=========================================================="
echo "Voterify Android Phone Debug Launcher — Google Pixel 9 Pro"
echo "AVD Name:       ${AVD_NAME}"
echo "Device Profile: ${DEVICE_PROFILE} (1280x2856, 495dpi, API 35)"
echo "=========================================================="

if command -v avdmanager >/dev/null 2>&1; then
  if ! avdmanager list avd | grep -q "${AVD_NAME}"; then
    echo "Creating AVD ${AVD_NAME} (${DEVICE_PROFILE})..."
    echo "no" | avdmanager create avd \
      --name "${AVD_NAME}" \
      --package "${SYS_IMAGE}" \
      --device "${DEVICE_PROFILE}" \
      --force
    cp "${SCRIPT_DIR}/avd-pixel-9-pro.ini" "${HOME}/.android/avd/${AVD_NAME}.avd/config.ini"
  fi
fi

if command -v emulator >/dev/null 2>&1; then
  echo "Starting ${AVD_NAME} emulator..."
  emulator -avd "${AVD_NAME}" -netdelay none -netspeed full &
fi

if command -v adb >/dev/null 2>&1; then
  echo "Waiting for Pixel 9 Pro device to come online..."
  adb wait-for-device
  echo "Launching Voterify Debug Activity (com.voterify.app/.MainActivity)..."
  adb shell am start -D -n "com.voterify.app/com.voterify.app.MainActivity"
fi
