# Sprint 031 — Validação pós-Beta

## Objetivo

Preparar o projeto Beta para a primeira abertura no Android Studio, reduzir os pontos mais comuns de falha de sincronização e fornecer um roteiro simples para produzir o primeiro APK de testes.

## Alterações de build

- `compileSdk` padronizado em API 36, evitando a exigência desnecessária do SDK 36.1.
- Gradle Wrapper mantido em 9.4.1, compatível com AGP 9.2.
- Tempo limite de download do Wrapper ampliado de 10 para 60 segundos.
- Cache local do Gradle ativado.
- AndroidX explicitamente ativado.
- Versão promovida para `1.0.0-beta02`, `versionCode 32`.

## Documentação criada

- `docs/GUIA_ANDROID_STUDIO_SPRINT031.md`
- `docs/ERROS_DE_SINCRONIZACAO_E_BUILD.md`

## Scripts criados

- `scripts/sync_and_validate.bat`
- `scripts/generate_beta_apk.bat`

## Validação disponível sem Gradle

O script `scripts/verify_project.py` verifica estrutura, Manifest, versão, rotas, Room, documentação e ausência de código temporário.

## Limitação do ambiente

A compilação completa não foi executada neste ambiente porque ele não possui acesso de rede para baixar o Gradle e as dependências Maven. O projeto foi preparado para essa validação no Android Studio do usuário.

## Resultado

O pacote está pronto para:

1. ser extraído no Windows;
2. ser aberto no Android Studio Quail;
3. sincronizar o Gradle;
4. executar testes unitários;
5. gerar `app-beta.apk`.
