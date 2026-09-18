#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p build-reports rc-package
./gradlew --no-daemon testDebugUnitTest lintRc assembleRc --stacktrace --warning-mode all 2>&1 | tee build-reports/rc1-build.log
cp app/build/outputs/apk/rc/app-rc.apk rc-package/LegacyMasterLiga-1.0.0-rc01.apk
sha256sum rc-package/LegacyMasterLiga-1.0.0-rc01.apk > rc-package/LegacyMasterLiga-1.0.0-rc01.sha256.txt
