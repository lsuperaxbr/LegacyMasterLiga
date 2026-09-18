@echo off
setlocal
cd /d "%~dp0\.."

echo ========================================
echo Legacy Master Liga - Build Beta
echo ========================================

call gradlew.bat clean testDebugUnitTest assembleDebug assembleBeta
if errorlevel 1 (
    echo.
    echo FALHA: verifique as mensagens acima.
    exit /b 1
)

echo.
echo Build concluido.
echo Debug: app\build\outputs\apk\debug\app-debug.apk
echo Beta:  app\build\outputs\apk\beta\app-beta.apk
endlocal
