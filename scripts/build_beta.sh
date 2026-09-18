#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."

printf '%s\n' '========================================'
printf '%s\n' 'Legacy Master Liga - Build Beta'
printf '%s\n' '========================================'

./gradlew clean testDebugUnitTest assembleDebug assembleBeta

printf '\n%s\n' 'Build concluído.'
printf '%s\n' 'Debug: app/build/outputs/apk/debug/app-debug.apk'
printf '%s\n' 'Beta:  app/build/outputs/apk/beta/app-beta.apk'
