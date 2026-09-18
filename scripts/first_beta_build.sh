#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build-reports
LOG="build-reports/sprint033-build.log"

echo "Legacy Master Liga - Primeira compilacao Beta"
java -version
python3 scripts/verify_project.py
set +e
./gradlew --no-daemon --stacktrace clean testDebugUnitTest assembleDebug assembleBeta 2>&1 | tee "$LOG"
RESULT=${PIPESTATUS[0]}
set -e
if [[ $RESULT -ne 0 ]]; then
  echo "BUILD FALHOU. Log: $LOG"
  exit "$RESULT"
fi
echo "APKs:"
echo "app/build/outputs/apk/debug/app-debug.apk"
echo "app/build/outputs/apk/beta/app-beta.apk"
