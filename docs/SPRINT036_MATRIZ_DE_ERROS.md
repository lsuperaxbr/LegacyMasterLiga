# Sprint 036 — matriz direta de erros

## `UnknownHostException: services.gradle.org`

O computador não conseguiu baixar o Gradle. Verifique internet, DNS, firewall, proxy ou antivírus e tente novamente.

## `SDK location not found`

Abra o projeto pelo Android Studio. Ele deverá criar `local.properties`. Se não criar, confira o caminho do SDK em **SDK Manager**.

## `Android SDK Platform 36 not found`

Abra **Tools > SDK Manager**, marque **Android API 36** e instale.

## `Android Gradle plugin requires Java 17`

Selecione JDK 17 nas configurações do Gradle.

## Erro contendo `Hilt`, `ComponentTreeDeps` ou `ScopedArtifact`

A Sprint 036 atualizou Hilt para 2.59.2, versão com correções específicas para AGP 9. Limpe o projeto e execute o script novamente.

## Erro contendo `Room cannot verify the data integrity`

Não desinstale nem apague dados antes de salvar a mensagem completa. Envie o log ao Legacy para revisar a migração indicada.

## Erro contendo `KSP`

Envie o log completo. Não troque Kotlin, KSP ou Room manualmente.

## `INSTALL_FAILED_UPDATE_INCOMPATIBLE`

Desinstale somente uma Beta antiga que tenha assinatura diferente e instale novamente. Faça backup antes caso existam dados importantes.
