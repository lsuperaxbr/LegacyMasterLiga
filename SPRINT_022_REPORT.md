# Sprint 022 — Centro de Estatísticas da Liga

## Base utilizada

Esta entrega foi aplicada diretamente sobre o último ZIP real disponível no projeto: `LegacyMasterLiga_Sprint020.zip`.
A resposta anterior que marcou a Sprint 021 como concluída não gerou um ZIP correspondente; portanto, esta entrega não declara o módulo de premiações configuráveis como implementado.

## Implementado

- Centro de Estatísticas com filtros por liga, competição e temporada.
- Resumo geral com partidas concluídas, total de gols, média de gols, clubes ativos, temporadas e movimentação do Mercado em CR.
- Ranking acumulado dos clubes com jogos, vitórias, empates, derrotas, pontos, gols pró, gols contra, saldo, média de pontos e movimentação em CR.
- Atualização reativa com Room, Flow e StateFlow.
- Abertura do perfil do clube a partir do ranking.
- Integração com Hilt, Navigation Compose e Dashboard.
- Nenhuma nova tabela ou migração foi necessária; o Room permanece na versão 10.

## Arquivos criados

- `core/database/dao/StatisticsDao.kt`
- `core/database/model/StatisticsRows.kt`
- `feature/statistics/domain/StatisticsModels.kt`
- `feature/statistics/domain/StatisticsRepository.kt`
- `feature/statistics/data/RoomStatisticsRepository.kt`
- `feature/statistics/presentation/StatisticsViewModel.kt`
- `feature/statistics/presentation/StatisticsScreen.kt`
- `SPRINT_022_REPORT.md`

## Arquivos modificados

- `core/database/AppDatabase.kt`
- `core/di/DatabaseModule.kt`
- `core/di/RepositoryModule.kt`
- `core/navigation/LegacyDestination.kt`
- `core/navigation/LegacyNavGraph.kt`
- `feature/dashboard/presentation/DashboardScreen.kt`
- `app/build.gradle.kts`
- `CHANGELOG.md`

## Versão

- `versionCode 22`
- `versionName 0.22.0-alpha22`
