@echo off
setlocal
cd /d "%~dp0.."
if not exist build-reports mkdir build-reports

echo Executando Android Lint da variante Beta...
call gradlew.bat lintBeta --warning-mode all --stacktrace > build-reports\beta-stabilization-02-lint.log 2>&1
set EXIT_CODE=%ERRORLEVEL%

type build-reports\beta-stabilization-02-lint.log

echo.
if %EXIT_CODE%==0 (
  echo Lint concluido com sucesso.
  echo Relatorios esperados em app\build\reports\
) else (
  echo Lint encontrou erros ou nao conseguiu concluir.
  echo Envie build-reports\beta-stabilization-02-lint.log para analise.
)
exit /b %EXIT_CODE%
