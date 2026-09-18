# Sprint 025 — Relatórios e Exportação

## Objetivo
Disponibilizar relatórios compartilháveis em PDF e CSV com filtros por liga, competição e temporada.

## Implementado
- Central de Relatórios em Jetpack Compose.
- Filtros encadeados de liga, competição e temporada.
- Relatório consolidado com classificação, financeiro em CR, mercado, resultados, histórico e estatísticas.
- Geração de PDF offline com `PdfDocument` e paginação automática.
- Geração de CSV UTF-8 organizado por seções.
- Salvamento no armazenamento escolhido pelo usuário via Storage Access Framework.
- Compartilhamento seguro por `FileProvider`, sem permissões amplas de armazenamento.
- Registro da exportação na Auditoria.
- Atalho de Relatórios no Dashboard.
- Integração com Room, Hilt e Navigation Compose.

## Observação de escopo dos filtros
Resultados, histórico e estatísticas respeitam liga, competição e temporada. Classificação é incluída quando uma temporada específica é selecionada. Como as tabelas atuais de Financeiro e Mercado são vinculadas à liga (e não diretamente a competição/temporada), essas seções respeitam o filtro de liga.

## Banco de dados
Nenhuma tabela ou migração nova. Room permanece na versão 12.

## Versão
- versionCode: 26
- versionName: 0.25.0-alpha25

## Validação
- Estrutura, imports, rotas, providers Hilt e manifesto verificados estaticamente.
- Tentativa de compilação executada; interrompida exclusivamente por indisponibilidade de acesso a `services.gradle.org` para baixar o Gradle Wrapper.
