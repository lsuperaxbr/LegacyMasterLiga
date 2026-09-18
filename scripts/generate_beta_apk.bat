@echo off
setlocal
cd /d "%~dp0\.."

echo ========================================
echo Legacy Master Liga - Gerar APK Beta
 echo ========================================

call gradlew.bat testDebugUnitTest assembleBeta --stacktrace
if errorlevel 1 (
    echo.
    echo FALHA: copie a primeira mensagem de erro do painel Build.
    exit /b 1
)

echo.
echo APK Beta criado em:
echo app\build\outputs\apk\beta\app-beta.apk
endlocal
