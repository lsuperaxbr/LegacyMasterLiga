@echo off
setlocal EnableExtensions
cd /d "%~dp0\.."

echo ==================================================
echo Legacy Master Liga - Preparacao do Android Studio
echo ==================================================
echo.

if not exist "settings.gradle.kts" (
  echo ERRO: abra/executa este script dentro da pasta raiz do projeto.
  exit /b 1
)

if not exist "gradle\wrapper\gradle-wrapper.jar" (
  echo ERRO: gradle-wrapper.jar nao foi encontrado.
  exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
  echo AVISO: Java nao foi encontrado no terminal do Windows.
  echo No Android Studio, selecione o JDK incorporado em:
  echo File ^> Settings ^> Build Tools ^> Gradle ^> Gradle JDK.
) else (
  echo Java encontrado:
  java -version
)

echo.
echo Verificacao estatica opcional...
where python >nul 2>nul
if errorlevel 1 (
  echo Python nao encontrado. A verificacao estatica sera ignorada.
  echo Isso NAO impede a sincronizacao nem a compilacao pelo Android Studio.
) else (
  python scripts\verify_project.py
  if errorlevel 1 exit /b 1
)

echo.
echo Preparacao concluida.
echo Agora abra no Android Studio a pasta que contem settings.gradle.kts.
echo Depois aguarde o Gradle Sync terminar.
endlocal
