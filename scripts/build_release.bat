@echo off
setlocal
cd /d "%~dp0.."
if not exist keystore.properties (
  echo [ERRO] Crie keystore.properties a partir de keystore.properties.example.
  exit /b 1
)
if not exist build-reports mkdir build-reports
call gradlew.bat --no-daemon testDebugUnitTest lintRelease assembleRelease --stacktrace --warning-mode all > build-reports\release-build.log 2>&1
if errorlevel 1 (
  echo [ERRO] Consulte build-reports\release-build.log
  exit /b 1
)
echo [OK] app\build\outputs\apk\release\app-release.apk
endlocal
