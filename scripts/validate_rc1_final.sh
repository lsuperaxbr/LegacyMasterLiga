#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build-reports rc-package

run_step() {
  local name="$1" task="$2" log="$3"
  echo "[RC1] $name..."
  if ! ./gradlew --no-daemon --stacktrace --warning-mode all "$task" >"$log" 2>&1; then
    echo "[ERRO] $name falhou. Consulte $log"
    exit 1
  fi
  echo "[OK] $name"
}

run_step "Testes unitários" testDebugUnitTest build-reports/rc1-tests.log
run_step "Android Lint RC" lintRc build-reports/rc1-lint.log
run_step "Compilação RC" assembleRc build-reports/rc1-assemble.log

apk="app/build/outputs/apk/rc/app-rc.apk"
[[ -f "$apk" ]] || { echo "[ERRO] APK RC não encontrado."; exit 1; }
cp "$apk" rc-package/LegacyMasterLiga-1.0.0-rc01.apk
sha256sum rc-package/LegacyMasterLiga-1.0.0-rc01.apk > rc-package/LegacyMasterLiga-1.0.0-rc01.sha256.txt
echo "[OK] rc-package/LegacyMasterLiga-1.0.0-rc01.apk"
