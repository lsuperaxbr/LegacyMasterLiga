#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build-reports
./gradlew --stop >/dev/null 2>&1 || true
set +e
./gradlew clean assembleBeta --profile --configuration-cache --warning-mode all --stacktrace \
  > build-reports/beta-stabilization-01.log 2>&1
result=$?
set -e
cat build-reports/beta-stabilization-01.log
if [ "$result" -eq 0 ]; then
  echo "BUILD BETA CONCLUIDO COM SUCESSO."
  echo "APK: app/build/outputs/apk/beta/app-beta.apk"
  echo "Perfil: build/reports/profile"
else
  echo "Envie build-reports/beta-stabilization-01.log"
fi
exit "$result"
