#!/usr/bin/env python3
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "app/src/main/java"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
STRINGS = ROOT / "app/src/main/res/values/strings.xml"

errors: list[str] = []
warnings: list[str] = []

try:
    ET.parse(MANIFEST)
except Exception as exc:
    errors.append(f"Manifest inválido: {exc}")

manifest_text = MANIFEST.read_text(encoding="utf-8")
if 'android:usesCleartextTraffic="false"' not in manifest_text:
    errors.append("usesCleartextTraffic=false ausente")
if 'android:allowBackup="false"' not in manifest_text:
    errors.append("allowBackup=false ausente")
if 'android:localeConfig="@xml/locales_config"' not in manifest_text:
    warnings.append("localeConfig não configurado")

try:
    ET.parse(STRINGS)
except Exception as exc:
    errors.append(f"strings.xml inválido: {exc}")

hardcoded = 0
null_descriptions = 0
for path in SRC.rglob("*.kt"):
    text = path.read_text(encoding="utf-8")
    hardcoded += len(re.findall(r'\bText\(\s*"', text))
    null_descriptions += text.count("contentDescription = null")
    if "TODO()" in text or "NotImplementedError" in text:
        errors.append(f"Código temporário encontrado: {path.relative_to(ROOT)}")

if hardcoded:
    warnings.append(f"Textos Compose literais ainda existentes: {hardcoded}")
if null_descriptions:
    warnings.append(
        f"Ícones decorativos com contentDescription=null: {null_descriptions} "
        "(revisar se todos são realmente decorativos)"
    )

print("REVISÃO ESTÁTICA DE LINT")
for item in errors:
    print(f"ERRO: {item}")
for item in warnings:
    print(f"AVISO: {item}")
print(f"Erros: {len(errors)} | Avisos: {len(warnings)}")
sys.exit(1 if errors else 0)
