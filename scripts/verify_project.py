#!/usr/bin/env python3
"""Static beta-readiness checks that do not require downloading Gradle."""
from __future__ import annotations

import re
import sys
import zipfile
from pathlib import Path
from xml.etree import ElementTree

ROOT = Path(__file__).resolve().parents[1]
ERRORS: list[str] = []
WARNINGS: list[str] = []


def require(condition: bool, message: str) -> None:
    if not condition:
        ERRORS.append(message)


def read(path: str) -> str:
    file = ROOT / path
    require(file.is_file(), f"Arquivo obrigatório ausente: {path}")
    return file.read_text(encoding="utf-8") if file.is_file() else ""


build = read("app/build.gradle.kts")
manifest_path = ROOT / "app/src/main/AndroidManifest.xml"
if manifest_path.is_file():
    try:
        ElementTree.parse(manifest_path)
    except ElementTree.ParseError as exc:
        ERRORS.append(f"AndroidManifest.xml inválido: {exc}")

version_code_match = re.search(r"versionCode\s*=\s*(\d+)", build)
version_name_match = re.search(r'versionName\s*=\s*"([^"]+)"', build)
require(version_code_match is not None and int(version_code_match.group(1)) >= 37, "versionCode Beta 7 ou superior não encontrado.")
require(version_name_match is not None and (version_name_match.group(1).startswith("1.0.0-beta") or version_name_match.group(1).startswith("1.0.0-rc")), "versionName Beta/RC válido não encontrado.")
require('android:allowBackup="false"' in read("app/src/main/AndroidManifest.xml"), "Backup automático do Android deve permanecer desativado.")

versions_catalog = read("gradle/libs.versions.toml")
require('hilt = "2.59.2"' in versions_catalog, "Hilt 2.59.2 não encontrado; esta versão corrige incompatibilidades conhecidas com AGP 9.")
require('agp = "9.2.1"' in versions_catalog, "AGP 9.2.1 não encontrado.")
require('ksp = "2.3.6"' in versions_catalog, "KSP 2.3.6 não encontrado.")
require('create("beta")' in build, "Variante Beta não encontrada no Gradle.")
require('create("rc")' in build, "Variante RC não encontrada no Gradle.")
require('isMinifyEnabled = true' in build and 'isShrinkResources = true' in build, "R8 e redução de recursos não estão habilitados para RC/Release.")
require((ROOT / "docs/RC1_ASSINATURA_E_BUILD.md").is_file(), "Guia de assinatura da RC1 ausente.")
require((ROOT / "scripts/build_rc1.bat").is_file(), "Script de build da RC1 ausente.")
require((ROOT / "app/proguard-rules.pro").is_file(), "Arquivo app/proguard-rules.pro ausente.")
require((ROOT / "docs/INSTALACAO_BETA.md").is_file(), "Documentação de instalação da Beta ausente.")
require((ROOT / "docs/PLANO_DE_TESTES_BETA.md").is_file(), "Plano de testes da Beta ausente.")
require((ROOT / "docs/GUIA_ANDROID_STUDIO_SPRINT031.md").is_file(), "Guia da Sprint 031 ausente.")
require((ROOT / "scripts/sync_and_validate.bat").is_file(), "Script de sincronização do Windows ausente.")

require((ROOT / "docs/SPRINT035_TESTE_NO_CELULAR.md").is_file(), "Guia de teste no celular da Sprint 035 ausente.")
require((ROOT / "docs/SPRINT035_CHECKLIST_PRIMEIRO_TESTE.md").is_file(), "Checklist da Sprint 035 ausente.")
require((ROOT / "docs/SPRINT035_COMO_REGISTRAR_ERROS.md").is_file(), "Guia de registro de erros da Sprint 035 ausente.")
require((ROOT / "scripts/package_beta_for_phone.bat").is_file(), "Script de pacote para celular ausente.")
require((ROOT / "scripts/sprint036_diagnostico_e_build.bat").is_file(), "Script final de diagnóstico e build da Sprint 036 ausente.")
require((ROOT / "docs/SPRINT036_PASSO_A_PASSO_FINAL.md").is_file(), "Guia final da Sprint 036 ausente.")

require((ROOT / "docs/SPRINT033_PRIMEIRA_COMPILACAO.md").is_file(), "Guia da Sprint 033 ausente.")
require((ROOT / "docs/SPRINT033_CORRECAO_ERROS.md").is_file(), "Matriz de correção da Sprint 033 ausente.")
require((ROOT / "scripts/first_beta_build.bat").is_file(), "Script de primeira compilação Beta ausente.")
require('compileSdk = 36' in build, "compileSdk 36 não encontrado.")
wrapper = read("gradle/wrapper/gradle-wrapper.properties")
require('gradle-9.4.1-bin.zip' in wrapper, "Gradle Wrapper 9.4.1 não encontrado.")
require('networkTimeout=60000' in wrapper, "Timeout estendido do Gradle Wrapper não encontrado.")
require(not (ROOT / "local.properties").exists(), "local.properties não deve ser distribuído no ZIP.")
require(not (ROOT / "keystore.properties").exists(), "keystore.properties privado não deve ser distribuído no ZIP.")

app_db = read("app/src/main/java/com/example/legacymasterliga/core/database/AppDatabase.kt")
version_match = re.search(r"version\s*=\s*(\d+)", app_db)
require(version_match is not None, "Versão do Room não localizada.")
if version_match:
    schema = ROOT / f"app/schemas/com.example.legacymasterliga.core.database.AppDatabase/{version_match.group(1)}.json"
    if not schema.is_file():
        WARNINGS.append(f"Schema Room {version_match.group(1)} ainda não foi gerado; ele será exportado pelo KSP na primeira compilação completa.")

routes = re.findall(r'override val route = "([^"]+)"', read("app/src/main/java/com/example/legacymasterliga/core/navigation/LegacyDestination.kt"))
require(len(routes) == len(set(routes)), "Existem rotas de navegação duplicadas.")

for source_root in (ROOT / "app/src/main/java", ROOT / "app/src/test", ROOT / "app/src/androidTest"):
    if not source_root.exists():
        continue
    for kotlin_file in source_root.rglob("*.kt"):
        text = kotlin_file.read_text(encoding="utf-8")
        require("TODO(" not in text and "NotImplementedError" not in text, f"Código temporário encontrado: {kotlin_file.relative_to(ROOT)}")
        require("\ufffd" not in text, f"Caractere inválido encontrado: {kotlin_file.relative_to(ROOT)}")

if WARNINGS:
    print("AVISOS")
    for warning in WARNINGS:
        print(f"- {warning}")

if ERRORS:
    print("FALHA NA VERIFICAÇÃO ESTÁTICA")
    for error in ERRORS:
        print(f"- {error}")
    sys.exit(1)

print("Verificação estática concluída com sucesso.")
print(f"Rotas únicas: {len(routes)}")
print(f"Room schema: versão {version_match.group(1) if version_match else '?'}")
print(f"Aplicativo: {version_name_match.group(1) if version_name_match else '?'} ({version_code_match.group(1) if version_code_match else '?'})")
