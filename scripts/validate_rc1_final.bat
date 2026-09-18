@echo off
setlocal EnableExtensions
cd /d "%~dp0.."

if not exist build-reports mkdir build-reports
if not exist rc-package mkdir rc-package

where java >nul 2>&1
if errorlevel 1 (
  echo [ERRO] Java nao encontrado. Configure o Gradle JDK 17 no Android Studio.
  exit /b 1
)

call :run_step "Testes unitarios" testDebugUnitTest build-reports\rc1-tests.log
if errorlevel 1 exit /b 1

call :run_step "Android Lint RC" lintRc build-reports\rc1-lint.log
if errorlevel 1 exit /b 1

call :run_step "Compilacao RC" assembleRc build-reports\rc1-assemble.log
if errorlevel 1 exit /b 1

if not exist app\build\outputs\apk\rc\app-rc.apk (
  echo [ERRO] O Gradle terminou, mas o APK nao foi encontrado.
  exit /b 1
)

copy /Y app\build\outputs\apk\rc\app-rc.apk rc-package\LegacyMasterLiga-1.0.0-rc01.apk >nul
certutil -hashfile rc-package\LegacyMasterLiga-1.0.0-rc01.apk SHA256 > rc-package\LegacyMasterLiga-1.0.0-rc01.sha256.txt

echo.
echo [OK] RC1 validada.
echo APK: rc-package\LegacyMasterLiga-1.0.0-rc01.apk
exit /b 0

:run_step
set STEP_NAME=%~1
set STEP_TASK=%~2
set STEP_LOG=%~3
echo.
echo [RC1] %STEP_NAME%...
call gradlew.bat --no-daemon --stacktrace --warning-mode all %STEP_TASK% > "%STEP_LOG%" 2>&1
if errorlevel 1 (
  echo [ERRO] %STEP_NAME% falhou. Consulte %STEP_LOG%
  exit /b 1
)
echo [OK] %STEP_NAME%
exit /b 0
