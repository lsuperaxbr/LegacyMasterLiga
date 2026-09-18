@echo off
setlocal
cd /d "%~dp0.."
if not exist build-reports mkdir build-reports

echo [RC1] Testes, Lint e APK RC...
call gradlew.bat --no-daemon testDebugUnitTest lintRc assembleRc --stacktrace --warning-mode all > build-reports\rc1-build.log 2>&1
if errorlevel 1 (
  echo [ERRO] Consulte build-reports\rc1-build.log
  exit /b 1
)

if not exist rc-package mkdir rc-package
copy /Y app\build\outputs\apk\rc\app-rc.apk rc-package\LegacyMasterLiga-1.0.0-rc01.apk >nul
certutil -hashfile rc-package\LegacyMasterLiga-1.0.0-rc01.apk SHA256 > rc-package\LegacyMasterLiga-1.0.0-rc01.sha256.txt

echo [OK] rc-package\LegacyMasterLiga-1.0.0-rc01.apk
endlocal
