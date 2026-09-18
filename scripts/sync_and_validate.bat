@echo off
setlocal
cd /d "%~dp0\.."

echo ========================================
echo Legacy Master Liga - Sincronizar e validar
echo ========================================

where python >nul 2>nul
if errorlevel 1 (
    echo AVISO: Python nao encontrado. A verificacao estatica sera ignorada.
) else (
    python scripts\verify_project.py
    if errorlevel 1 exit /b 1
)

call gradlew.bat --version
if errorlevel 1 (
    echo.
    echo FALHA ao iniciar o Gradle Wrapper.
    echo Verifique internet, firewall e a configuracao Gradle JDK do Android Studio.
    exit /b 1
)

call gradlew.bat help --stacktrace
if errorlevel 1 exit /b 1

echo.
echo Projeto sincronizado e configuracao Gradle validada.
endlocal
