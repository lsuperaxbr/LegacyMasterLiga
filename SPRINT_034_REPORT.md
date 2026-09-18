# Sprint 034 — Primeira abertura no Android Studio

## Objetivo

Preparar a abertura inicial, a sincronização do Gradle e a geração do primeiro APK Beta sem adicionar funcionalidades.

## Alterações

- versão promovida para `1.0.0-beta05` (`versionCode 35`);
- roteiro extremamente simples de abertura e build;
- matriz de riscos preventivos;
- script de preparação que não exige Python;
- script robusto para gerar o APK Beta e salvar o log completo;
- verificador estático atualizado para aceitar a numeração atual da Beta.

## Decisões preventivas

- o primeiro build executa sem Configuration Cache;
- nenhuma dependência ou versão foi alterada sem necessidade;
- `local.properties` continua fora do ZIP;
- nenhuma funcionalidade existente foi removida.

## Limitação do ambiente

A compilação Gradle completa não pôde ser executada neste ambiente porque a distribuição do Wrapper não está disponível localmente e o acesso externo ao `services.gradle.org` é bloqueado. O projeto inclui os scripts e o roteiro para a compilação no Android Studio.
