# Sprint 008 — Inscrições e Participantes

## Objetivo
Permitir selecionar os clubes que disputarão cada competição e temporada, impedir duplicidades e preparar a geração automática de rodadas.

## Implementado
- Entidade `CompetitionParticipantEntity` ligada a `SeasonEntity` e `ClubEntity`.
- Restrição única `(seasonId, clubId)` para impedir inscrições duplicadas.
- Exclusão em cascata ao remover temporada e proteção do histórico de clubes.
- DAO reativo com `Flow`.
- Repository Room com seleção/remoção segura.
- ViewModel Hilt com seleção de liga, competição/temporada e participantes.
- Tela Jetpack Compose para administrar inscrições.
- Banco da Liga filtrado automaticamente.
- Integração com Dashboard e Navigation Compose.
- Migração Room da versão 2 para a versão 3.

## Preparação para a próxima sprint
A tabela de participantes fornece a lista oficial de clubes de cada temporada. A Sprint 009 poderá usar essa lista para gerar rodadas e partidas automaticamente, respeitando o formato somente ida ou ida e volta.

## Arquivos criados
- `core/database/entity/CompetitionParticipantEntity.kt`
- `core/database/dao/CompetitionParticipantDao.kt`
- `feature/participants/domain/ParticipantModels.kt`
- `feature/participants/domain/ParticipantRepository.kt`
- `feature/participants/data/RoomParticipantRepository.kt`
- `feature/participants/presentation/ParticipantsViewModel.kt`
- `feature/participants/presentation/ParticipantsScreen.kt`
- `SPRINT_008_REPORT.md`

## Arquivos modificados
- `core/database/AppDatabase.kt`
- `core/di/DatabaseModule.kt`
- `core/di/RepositoryModule.kt`
- `core/navigation/LegacyDestination.kt`
- `core/navigation/LegacyNavGraph.kt`
- `feature/dashboard/presentation/DashboardScreen.kt`
- `app/build.gradle.kts`
- `CHANGELOG.md`
