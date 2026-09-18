# Sprint 030 — Preparação da primeira Beta

## Objetivo

Preparar o Legacy Master Liga para a primeira rodada de testes reais, sem remover nenhuma funcionalidade existente.

## Alterações realizadas

- Versão promovida de `0.29.0-alpha29` para `1.0.0-beta01` (`versionCode 31`).
- Adicionada variante `beta`, com sufixo de aplicativo próprio para instalação paralela.
- Adicionada configuração opcional de assinatura Release por `keystore.properties`.
- Adicionado `proguard-rules.pro`, que estava referenciado pelo Gradle e ausente no ZIP anterior.
- Release mantido sem minificação durante a Beta para reduzir riscos de regressão.
- Adicionados arquivos de documentação de instalação, testes, assinatura e entrega.
- Adicionados scripts de build para Windows e sistemas Unix.
- Atualizado `.gitignore` para proteger chaves e pacotes gerados.
- Preservados Room 12, migrações, módulos, permissões, navegação e testes existentes.

## Variantes

- `debug`: diagnóstico e desenvolvimento.
- `beta`: testes em celulares reais, com `applicationIdSuffix = .beta`.
- `release`: preparada para assinatura quando o arquivo privado for configurado.

## Validação executada

- Verificação estática do projeto com `scripts/verify_project.py`.
- Validação do Manifest XML.
- Verificação de referências a arquivos Gradle e ProGuard.
- Integridade do ZIP.

## Limitação

Este ambiente não possui o Gradle 9.4.1 nem acesso ao `services.gradle.org`; por isso, não foi possível gerar o APK nesta execução. A documentação contém os comandos exatos para a compilação no Android Studio.

## Estado

Projeto preparado para sincronização, compilação e testes da Beta 1 em um ambiente Android Studio com internet.
