# Sprint 029 — Revisão final e preparação para Beta

## Objetivo

Revisar a base completa do Legacy Master Liga antes da geração da Beta, corrigindo inconsistências detectáveis sem acesso ao download do Gradle e preservando todos os módulos implementados.

## Correções realizadas

- Corrigida a inferência numérica de `Int` para `Long` nos fluxos de Financeiro, Mercado e Premiações.
- Desativado o backup automático do Android, evitando cópias paralelas do banco fora do sistema oficial `.lmlbackup`.
- Bloqueado tráfego HTTP sem criptografia por `usesCleartextTraffic=false`.
- Ativado o callback moderno de navegação Voltar do Android.
- Mantida a cadeia completa de migrações Room da versão 1 até a 12.
- Verificadas as rotas protegidas por perfil e a ausência de rotas duplicadas.
- Verificados os bindings de Repositories e DAOs no Hilt.
- Verificada a integridade estrutural dos testes unitários e instrumentados.
- Adicionado `scripts/verify_project.py` para inspeção estática sem depender da internet ou do Gradle.
- Atualizada a versão para `0.29.0-alpha29` (`versionCode 30`).

## Segurança e dados

O backup oficial permanece sendo o pacote exportável `.lmlbackup`, com manifesto e checksum. O Android Auto Backup foi desativado para evitar restaurações parciais ou incompatíveis do banco Room.

## Validação executada

- XML do Manifest validado.
- Rotas únicas verificadas.
- Diretório de schemas Room verificado. O schema 12 será gerado pelo KSP na primeira compilação completa; o ZIP recebido continha apenas os schemas 6 e 7.
- Ausência de `TODO()` e `NotImplementedError` no código de produção e testes.
- Imports internos, DAOs e bindings Hilt revisados estaticamente.
- Integridade do ZIP final verificada.

## Limitação conhecida

A compilação Gradle completa continua dependendo do download do Gradle Wrapper e das dependências Android. Este ambiente não possui acesso a `services.gradle.org`; portanto, a confirmação final de compilação ocorrerá quando o projeto for aberto em um Android Studio com as dependências disponíveis.

## Estado

O projeto está preparado para a Sprint 030, dedicada à geração e validação da primeira Beta.
