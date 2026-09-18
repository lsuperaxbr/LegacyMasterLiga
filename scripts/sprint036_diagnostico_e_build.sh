#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build-reports beta-package
LOG="build-reports/sprint036-build.log"
INFO="build-reports/sprint036-ambiente.txt"
{
  echo "LEGACY MASTER LIGA - DIAGNOSTICO SPRINT 036"
  date
  pwd
  java -version
} >"$INFO" 2>&1
chmod +x gradlew
./gradlew --stop >>"$LOG" 2>&1 || true
if ! ./gradlew --no-configuration-cache --stacktrace --warning-mode all clean testDebugUnitTest assembleBeta >"$LOG" 2>&1; then
  echo "A compilação falhou. Envie o arquivo $LOG ao Legacy."
  exit 1
fi
APK="app/build/outputs/apk/beta/app-beta.apk"
if [[ ! -f "$APK" ]]; then
  echo "APK não encontrado em $APK. Envie $LOG ao Legacy."
  exit 2
fi
cp "$APK" "beta-package/LegacyMasterLiga-1.0.0-beta07.apk"
sha256sum "beta-package/LegacyMasterLiga-1.0.0-beta07.apk" > "beta-package/LegacyMasterLiga-1.0.0-beta07.sha256.txt"
echo "APK gerado em beta-package/LegacyMasterLiga-1.0.0-beta07.apk"
