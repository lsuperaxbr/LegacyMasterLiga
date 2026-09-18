# Sprint 010 — Resultados e Classificação Automática

## Objetivo
Permitir lançar e corrigir placares, recalculando integralmente a classificação da temporada com segurança.

## Implementado
- Lançamento e correção de resultados diretamente na tela de rodadas.
- Validação de placares entre 0 e 99.
- Recalculo transacional da tabela após cada alteração.
- Jogos, vitórias, empates, derrotas, pontos, gols pró, gols contra e saldo de gols.
- Aproveitamento calculado na apresentação: pontos / (jogos × 3) × 100.
- Critérios de ordenação: pontos, vitórias, saldo de gols, gols pró e nome.
- Líder destacado em amarelo com coroa.
- Atualização do estado da rodada para AGENDADA, EM ANDAMENTO ou FINALIZADA.
- Nova tela reativa de Classificação integrada ao Dashboard.
- Migração Room 4 → 5 para a tabela `standings`.

## Segurança da correção
A classificação não é ajustada por diferença. Ela é reconstruída a partir de todas as partidas finalizadas da temporada, evitando duplicação de pontos ao corrigir um placar.

## Arquivos criados
- `core/database/entity/StandingEntity.kt`
- `core/database/dao/StandingDao.kt`
- `core/database/model/StandingRow.kt`
- `feature/results/domain/ResultsModels.kt`
- `feature/results/domain/ResultsRepository.kt`
- `feature/results/data/RoomResultsRepository.kt`
- `feature/results/presentation/StandingsViewModel.kt`
- `feature/results/presentation/StandingsScreen.kt`

## Arquivos modificados
- `core/database/AppDatabase.kt`
- `core/database/dao/MatchDao.kt`
- `core/database/dao/RoundDao.kt`
- `core/di/DatabaseModule.kt`
- `core/di/RepositoryModule.kt`
- `core/navigation/LegacyDestination.kt`
- `core/navigation/LegacyNavGraph.kt`
- `feature/dashboard/presentation/DashboardScreen.kt`
- `feature/schedule/presentation/ScheduleScreen.kt`
- `feature/schedule/presentation/ScheduleViewModel.kt`
- `app/build.gradle.kts`
- `CHANGELOG.md`
