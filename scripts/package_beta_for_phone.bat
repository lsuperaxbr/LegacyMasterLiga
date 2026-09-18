@echo off
setlocal EnableExtensions
cd /d "%~dp0\.."

set APK=app\build\outputs\apk\beta\app-beta.apk
set OUT=beta-package
set TARGET=%OUT%\LegacyMasterLiga-1.0.0-beta06.apk

if not exist "%APK%" (
  echo ERRO: APK Beta nao encontrado.
  echo Gere primeiro com: scripts\generate_first_beta_apk.bat
  exit /b 1
)

if not exist "%OUT%" mkdir "%OUT%"
copy /Y "%APK%" "%TARGET%" >nul
copy /Y "docs\SPRINT035_TESTE_NO_CELULAR.md" "%OUT%\LEIA-ME-TESTE-NO-CELULAR.md" >nul
copy /Y "docs\SPRINT035_CHECKLIST_PRIMEIRO_TESTE.md" "%OUT%\CHECKLIST-PRIMEIRO-TESTE.md" >nul
copy /Y "BETA_TEST_REPORT_TEMPLATE.md" "%OUT%\RELATORIO-DE-TESTE.md" >nul

certutil -hashfile "%TARGET%" SHA256 > "%OUT%\SHA256.txt" 2>nul

echo ==============================================
echo PACOTE PARA O CELULAR CRIADO
ECHO %TARGET%
echo ==============================================
endlocal
