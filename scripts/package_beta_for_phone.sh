#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
APK="app/build/outputs/apk/beta/app-beta.apk"
OUT="beta-package"
TARGET="$OUT/LegacyMasterLiga-1.0.0-beta06.apk"
if [ ! -f "$APK" ]; then
  echo "ERRO: APK Beta não encontrado."
  echo "Gere primeiro com: scripts/generate_first_beta_apk.sh ou ./gradlew assembleBeta"
  exit 1
fi
mkdir -p "$OUT"
cp "$APK" "$TARGET"
cp docs/SPRINT035_TESTE_NO_CELULAR.md "$OUT/LEIA-ME-TESTE-NO-CELULAR.md"
cp docs/SPRINT035_CHECKLIST_PRIMEIRO_TESTE.md "$OUT/CHECKLIST-PRIMEIRO-TESTE.md"
cp BETA_TEST_REPORT_TEMPLATE.md "$OUT/RELATORIO-DE-TESTE.md"
if command -v sha256sum >/dev/null 2>&1; then sha256sum "$TARGET" > "$OUT/SHA256.txt"; fi
echo "Pacote criado em: $OUT"
