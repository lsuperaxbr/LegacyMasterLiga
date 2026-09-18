@echo off
setlocal EnableExtensions
cd /d "%~dp0\.."

if not exist build-reports mkdir build-reports
set LOG=build-reports\sprint034-beta-build.log

echo ==============================================
echo Legacy Master Liga - Gerar primeiro APK Beta
echo ==============================================
echo.

where java >nul 2>nul
if errorlevel 1 (
  echo ERRO: Java nao foi encontrado neste terminal.
  echo Execute pelo Terminal do Android Studio com o JDK incorporado selecionado.
  exit /b 1
)

echo A primeira execucao pode demorar porque o Gradle e as dependencias serao baixados.
echo Log: %LOG%
echo.

call gradlew.bat --no-daemon --no-configuration-cache --stacktrace clean testDebugUnitTest assembleBeta > "%LOG%" 2>&1
set RESULT=%ERRORLEVEL%

type "%LOG%"

if not "%RESULT%"=="0" (
  echo.
  echo ==============================================
  echo A COMPILACAO FALHOU
  echo Envie o arquivo: %LOG%
  echo Ou envie a primeira parte depois de "What went wrong".
  echo ==============================================
  exit /b %RESULT%
)

echo.
echo ==============================================
echo APK BETA GERADO COM SUCESSO
echo app\build\outputs\apk\beta\app-beta.apk
echo ==============================================
endlocal
