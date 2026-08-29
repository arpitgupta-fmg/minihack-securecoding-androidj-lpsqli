#!/bin/bash
# Local macOS equivalent of the lab's `run tests` (/usr/local/bin/tests.sh).
# Runs the instrumented tests on a connected emulator/device and prints
# Passed / Failed / Total exactly like the lab grader.
#
# Prereqisites: an Android emulator (or USB device) must be running.
#   Check with:  adb devices   (expect a line ending in "device")

set -u
set -o pipefail

# Always operate from the directory this script lives in (the project root).
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR" || { echo "Could not enter project: $PROJECT_DIR"; exit 1; }

LOG_FILE="/tmp/momofactory-test-output.log"
RESULT_DIR="app/build/outputs/androidTest-results/connected"

if [ ! -x "./gradlew" ]; then
    chmod +x ./gradlew 2>/dev/null || { echo "gradlew not found/executable"; exit 1; }
fi

# Fail early with a clear message if no device is connected.
if ! command -v adb >/dev/null 2>&1; then
    echo "adb not on PATH. Add \$ANDROID_HOME/platform-tools to PATH."; exit 1
fi
if ! adb devices | awk 'NR>1 && $2=="device"{found=1} END{exit found?0:1}'; then
    echo "No emulator/device connected. Start one, then: adb devices"
    echo "Passed: 0"; echo "Failed: 0"; echo "Total: 0/0"
    exit 1
fi

rm -f "$LOG_FILE"
rm -rf "$RESULT_DIR"

./gradlew connectedAndroidTest --quiet >"$LOG_FILE" 2>&1
GRADLE_STATUS=$?

PASSED=0; FAILED=0; ERRORS=0; TOTAL=0

if [ -d "$RESULT_DIR" ]; then
    TOTAL=$(grep -Rho 'tests="[0-9]*"' "$RESULT_DIR" 2>/dev/null \
        | awk -F'"' '{ t += $2 } END { print t + 0 }')
    FAILED=$(grep -Rho 'failures="[0-9]*"' "$RESULT_DIR" 2>/dev/null \
        | awk -F'"' '{ t += $2 } END { print t + 0 }')
    ERRORS=$(grep -Rho 'errors="[0-9]*"' "$RESULT_DIR" 2>/dev/null \
        | awk -F'"' '{ t += $2 } END { print t + 0 }')
    FAILED=$((FAILED + ERRORS))
    PASSED=$((TOTAL - FAILED))
    [ "$PASSED" -lt 0 ] && PASSED=0
fi

echo "Passed: $PASSED"
echo "Failed: $FAILED"
echo "Total: $PASSED/$TOTAL"

if [ "$TOTAL" -eq 0 ]; then
    echo "(No results parsed — see $LOG_FILE for the Gradle output.)"
fi

exit "$GRADLE_STATUS"
