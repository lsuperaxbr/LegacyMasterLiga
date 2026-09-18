@echo off
setlocal EnableExtensions
cd /d "%~dp0\.."

if not exist build-reports mkdir build-reports
set LOG=build-reports\sprint033-build.log

echo ================================================
echo Legacy Master Liga - Primeira compilacao Beta
echo ================================================
echo.

echo [1/4] Verificando Java...
where java >nul 2>nul
if errorlevel 1 (
  echo ERRO: Java nao foi encontrado.
  echo No Android Studio, use o JDK incorporado ^(Embedded JDK 17 ou superior^).
  exit /b 1
)
java -version

echo.
echo [2/4] Verificacao estatica...
python scripts\verify_project.py
if errorlevel 1 exit /b 1

echo.
echo [3/4] Limpando e executando testes/build...
call gradlew.bat --no-daemon --stacktrace clean testDebugUnitTest assembleDebug assembleBeta > "%LOG%" 2>&1
set RESULT=%ERRORLEVEL%

type "%LOG%"

if not "%RESULT%"=="0" (
  echo.
  echo ================================================
  echo BUILD FALHOU
  echo Log completo: %LOG%
  echo Envie o arquivo de log ou a primeira mensagem "What went wrong".
  echo ================================================
  exit /b %RESULT%
)

echo.
echo [4/4] APKs gerados com sucesso:
echo app\build\outputs\apk\debug\app-debug.apk
echo app\build\outputs\apk\beta\app-beta.apk
echo.
echo BUILD SUCCESSFUL
endlocal
