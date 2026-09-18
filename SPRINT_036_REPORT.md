# Sprint 036 — Preparação final de compilação e instalação

## Objetivo

Preparar o projeto para a primeira compilação e instalação reais, corrigindo apenas riscos de build e criando um fluxo único de diagnóstico.

## Alterações

- versão promovida para `1.0.0-beta07` (`versionCode 37`);
- Hilt atualizado de `2.59` para `2.59.2`, corrigindo riscos conhecidos de compatibilidade com AGP 9;
- script único de diagnóstico, testes e geração do APK para Windows;
- equivalente para Linux/macOS;
- guia final de abertura, compilação e instalação;
- matriz direta de erros;
- verificador estático ampliado para conferir AGP, KSP, Hilt e os artefatos da Sprint 036.

## Validação disponível neste ambiente

- verificação estática completa;
- XML do Manifest validado;
- rotas verificadas;
- versão Room conferida;
- tentativa real de iniciar o Gradle Wrapper.

A execução do Gradle não avançou devido a `UnknownHostException` ao acessar `services.gradle.org`, limitação de rede deste ambiente.

## Funcionalidades

Nenhuma funcionalidade foi adicionada ou removida.
