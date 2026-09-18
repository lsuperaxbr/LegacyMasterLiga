# Sprint 002 — Infraestrutura Room

## Objetivo

Implementar a primeira versão real do banco de dados local do Legacy Master Liga sem recriar o projeto.

## Entregue

- `AppDatabase` versão 1 com exportação de esquema.
- `LegacyTypeConverters`.
- Entidades iniciais: usuário, sessão, liga, clube, competição e temporada.
- DAOs iniciais com `suspend` e `Flow`.
- `DatabaseModule` do Hilt.
- Chaves estrangeiras, índices e restrições únicas.

## Regras já representadas

- Moeda oficial padrão: `CR`.
- Mais de uma liga pode existir no mesmo aplicativo.
- Clubes são separados por liga.
- O Banco da Liga é um clube invisível (`isBank`) e não precisa ter elenco cadastrado.
- Competições pertencem a uma liga e podem ser Liga, Copa ou Supercopa.
- Cada competição define seu formato: ida, ida e volta ou jogo único.
- Temporadas ficam separadas por competição, evitando mistura de placares.
- Sessões são vinculadas a usuários e removidas em cascata quando o usuário é removido.

## Validação

A tarefa Gradle `:app:compileDebugKotlin` foi iniciada, mas o ambiente não possui acesso ao host `services.gradle.org` para baixar o Gradle 9.4.1. Não houve falha de código identificada por compilação; a compilação final deve ser executada no Android Studio conectado à internet.
