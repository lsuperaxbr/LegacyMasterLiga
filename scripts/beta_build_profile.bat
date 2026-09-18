@echo off
setlocal
cd /d "%~dp0\.."
if not exist build-reports mkdir build-reports

echo ============================================================
echo Legacy Master Liga - Perfil local de compilacao Beta
echo ============================================================
echo.
echo Este comando nao envia dados para a internet.
echo O relatorio HTML sera criado em build\reports\profile.
echo.

call gradlew.bat --stop >nul 2>&1
call gradlew.bat clean assembleBeta --profile --configuration-cache --warning-mode all --stacktrace > "build-reports\beta-stabilization-01.log" 2>&1
set RESULT=%ERRORLEVEL%

type "build-reports\beta-stabilization-01.log"

echo.
if %RESULT% EQU 0 (
  echo BUILD BETA CONCLUIDO COM SUCESSO.
  echo APK: app\build\outputs\apk\beta\app-beta.apk
  echo Perfil: build\reports\profile
) else (
  echo O build encontrou erro. Envie o arquivo:
  echo build-reports\beta-stabilization-01.log
)
exit /b %RESULT%
