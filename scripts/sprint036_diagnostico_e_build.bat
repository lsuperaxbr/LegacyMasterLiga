@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0.."

if not exist "build-reports" mkdir "build-reports"
set "LOG=build-reports\sprint036-build.log"
set "INFO=build-reports\sprint036-ambiente.txt"

> "%INFO%" echo LEGACY MASTER LIGA - DIAGNOSTICO SPRINT 036
>>"%INFO%" echo Data: %date% %time%
>>"%INFO%" echo Pasta: %CD%
>>"%INFO%" echo.

where java >>"%INFO%" 2>&1
if errorlevel 1 (
  echo ERRO: Java nao encontrado.
  echo Abra o Android Studio e selecione o JDK 17 em Settings ^> Build Tools ^> Gradle.
  pause
  exit /b 10
)
java -version >>"%INFO%" 2>&1

if not exist "gradlew.bat" (
  echo ERRO: gradlew.bat nao encontrado. Abra a pasta raiz correta do projeto.
  pause
  exit /b 11
)

if not exist "app\build.gradle.kts" (
  echo ERRO: app\build.gradle.kts nao encontrado.
  pause
  exit /b 12
)

if exist "local.properties" (
  >>"%INFO%" echo local.properties encontrado.
) else (
  >>"%INFO%" echo local.properties ainda nao existe. O Android Studio deve cria-lo automaticamente.
)

call gradlew.bat --stop >>"%LOG%" 2>&1
call gradlew.bat --no-configuration-cache --stacktrace --warning-mode all clean testDebugUnitTest assembleBeta >"%LOG%" 2>&1
set "CODE=%ERRORLEVEL%"

if not "%CODE%"=="0" (
  echo.
  echo A COMPILACAO ENCONTROU UM ERRO.
  echo Envie este arquivo para o Legacy:
  echo %CD%\%LOG%
  echo.
  echo Nao apague o projeto e nao altere arquivos aleatoriamente.
  pause
  exit /b %CODE%
)

set "APK=app\build\outputs\apk\beta\app-beta.apk"
if not exist "%APK%" (
  echo O Gradle terminou, mas o APK nao foi localizado em:
  echo %CD%\%APK%
  echo Envie o log: %CD%\%LOG%
  pause
  exit /b 13
)

if not exist "beta-package" mkdir "beta-package"
copy /y "%APK%" "beta-package\LegacyMasterLiga-1.0.0-beta07.apk" >nul

certutil -hashfile "beta-package\LegacyMasterLiga-1.0.0-beta07.apk" SHA256 > "beta-package\LegacyMasterLiga-1.0.0-beta07.sha256.txt" 2>&1

echo.
echo ==============================================
echo APK BETA GERADO COM SUCESSO
echo ==============================================
echo %CD%\beta-package\LegacyMasterLiga-1.0.0-beta07.apk
echo.
start "" "%CD%\beta-package"
pause
exit /b 0
