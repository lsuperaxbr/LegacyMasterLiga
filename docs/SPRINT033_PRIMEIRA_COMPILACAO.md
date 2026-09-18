# Sprint 033 — primeira compilação real da Beta

## Objetivo

Sincronizar o projeto no Android Studio, executar os testes disponíveis e produzir o primeiro APK Beta instalável, sem incluir novas funcionalidades.

## Requisitos confirmados

- Android Studio Quail 1 | 2026.1.1 Patch 2 ou mais recente.
- JDK 17 (use o JDK incorporado do Android Studio).
- Android SDK Platform 36 e Build Tools 36.0.0.
- Internet na primeira sincronização para baixar Gradle e dependências.

## Abrir corretamente

1. Extraia o ZIP para `C:\LegacyMasterLiga`.
2. No Android Studio, clique em **Open**.
3. Selecione a pasta `LegacyMasterLiga`, onde está o arquivo `settings.gradle.kts`.
4. Confirme **Trust Project**.
5. Aguarde o término de **Gradle Sync**.

## Configurar o JDK

Abra:

`File > Settings > Build, Execution, Deployment > Build Tools > Gradle`

Em **Gradle JDK**, escolha o JDK incorporado do Android Studio, versão 17 ou superior.

## Gerar automaticamente

No Terminal do Android Studio, execute:

```bat
scripts\first_beta_build.bat
```

O script executa:

```text
clean
testDebugUnitTest
assembleDebug
assembleBeta
```

## Local do APK

```text
app\build\outputs\apk\beta\app-beta.apk
```

O APK Debug também será criado em:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Login inicial

```text
Usuário: admin
Senha: admin123
```

## Quando ocorrer erro

O log completo será salvo em:

```text
build-reports\sprint033-build.log
```

Envie esse arquivo ou copie a primeira seção `What went wrong`. Não altere várias dependências ao mesmo tempo.
