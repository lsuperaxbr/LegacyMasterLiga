#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build-reports
./gradlew lintBeta --warning-mode all --stacktrace 2>&1 | tee build-reports/beta-stabilization-02-lint.log
