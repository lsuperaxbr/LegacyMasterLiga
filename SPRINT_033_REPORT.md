# Sprint 033 — Preparação da primeira compilação real

## Escopo

Revisão da configuração Gradle, KSP, Room, Hilt e Compose, criação de processo reproduzível para a primeira compilação e documentação de correção de erros, sem novas funcionalidades.

## Alterações

- versão elevada para `1.0.0-beta04` (`versionCode 34`);
- removido o plugin Foojay, que não era utilizado e acrescentava uma resolução externa desnecessária durante a sincronização;
- criados scripts de compilação diagnóstica para Windows e Linux/macOS;
- criado log persistente em `build-reports/sprint033-build.log`;
- criados guias de primeira compilação e resolução de erros;
- preservadas as 23 rotas, Room 12 e todas as funcionalidades da Beta 03.

## Compatibilidade revisada

- AGP 9.2.1 / Gradle 9.4.1 / JDK 17;
- compileSdk e targetSdk 36;
- Room 2.8.4;
- Hilt 2.59 com suporte a AGP 9;
- Navigation Compose 2.9.8;
- Kotlin 2.2.10 e KSP 2.3.6.

## Limitação da validação neste ambiente

A tentativa de execução do Gradle Wrapper falhou com `UnknownHostException` porque este ambiente não resolve `services.gradle.org`. A verificação estática foi concluída. A compilação executável será confirmada no Android Studio com internet.
