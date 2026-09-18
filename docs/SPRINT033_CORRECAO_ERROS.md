# Sprint 033 — correção direta dos erros de compilação

## Gradle não baixa

**Sintomas:** `UnknownHostException`, timeout ou falha em `services.gradle.org`.

1. Confirme que o Android Studio está em modo online.
2. Desative `Offline work` nas configurações do Gradle.
3. Teste a conexão sem VPN ou proxy corporativo.
4. Clique em **Sync Project with Gradle Files** novamente.

## JDK incompatível

**Sintomas:** mensagens sobre Java, JVM ou versão de bytecode.

Selecione o JDK incorporado no Android Studio. O projeto usa Java 17 e o Android Gradle Plugin 9.2 requer JDK 17.

## SDK 36 ausente

Abra **Tools > SDK Manager**, marque **Android 16 / API 36** e instale o SDK Platform e Build Tools.

## Erro KSP ou Room

1. Faça **Build > Clean Project**.
2. Execute `gradlew.bat clean kspBetaKotlin assembleBeta --stacktrace`.
3. Não apague as migrações Room.
4. Após uma compilação bem-sucedida, confirme a criação do schema 12 em `app/schemas`.

## Erro Hilt

Procure primeiro a classe citada imediatamente antes de `ComponentProcessingStep`. Normalmente a causa é uma dependência ausente, um construtor não injetável ou um binding duplicado.

## Erro Compose

Copie o primeiro arquivo e a primeira linha indicados pelo compilador. Erros seguintes frequentemente são consequências do primeiro.

## Resultado esperado

A Sprint 033 só estará validada em execução quando o comando terminar com `BUILD SUCCESSFUL` e gerar `app-beta.apk`.
