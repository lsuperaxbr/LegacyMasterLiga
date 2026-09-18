# Sprint 019 — Hall da Fama e Recordes Históricos

## Objetivo

Criar uma área permanente para consultar os principais recordes históricos de cada liga, calculados diretamente a partir das temporadas, classificações, partidas e transferências já registradas.

## Implementado

- Hall da Fama filtrado por liga.
- Maior campeão, considerando temporadas encerradas ou arquivadas.
- Clube com mais vitórias acumuladas.
- Melhor ataque registrado em uma temporada.
- Melhor defesa registrada em uma temporada encerrada.
- Maior goleada, com placar, rodada, competição e temporada.
- Maior sequência invicta calculada cronologicamente e reiniciada por temporada.
- Clube com maior movimentação total em CR no Mercado.
- Abertura do perfil do clube diretamente pelos cartões de recordes.
- Integração com Room, Hilt, Navigation Compose e Dashboard.
- Atualização reativa por `Flow` e `StateFlow`.

## Banco de dados

Nenhuma nova tabela foi necessária. A Sprint adiciona apenas consultas agregadas por meio de `HallOfFameDao`, reutilizando o banco Room na versão 9.

## Arquivos criados

- `core/database/dao/HallOfFameDao.kt`
- `core/database/model/HallOfFameRows.kt`
- `feature/halloffame/domain/HallOfFameModels.kt`
- `feature/halloffame/domain/HallOfFameRepository.kt`
- `feature/halloffame/data/RoomHallOfFameRepository.kt`
- `feature/halloffame/presentation/HallOfFameViewModel.kt`
- `feature/halloffame/presentation/HallOfFameScreen.kt`

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

- `versionCode`: 19
- `versionName`: `0.19.0-alpha19`
