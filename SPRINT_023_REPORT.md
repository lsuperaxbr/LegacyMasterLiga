# Sprint 023 — Dashboard Avançado da Liga

## Objetivo
Reunir em uma única tela os dados mais importantes da liga, respeitando as permissões de cada perfil.

## Implementação
- Contexto reativo da liga, competição e temporada ativa.
- Líder atual e pontuação.
- Próxima rodada com quantidade de jogos pendentes.
- Últimos três resultados.
- Últimas três transferências em CR.
- Últimas três notícias automáticas.
- Saldo do clube associado ao Presidente.
- Total de CR em circulação para Administradores.
- Atalhos contextuais por perfil.

## Arquivos criados
- `core/database/dao/DashboardDao.kt`
- `core/database/model/DashboardRows.kt`

## Arquivos modificados
- `core/database/AppDatabase.kt`
- `core/di/DatabaseModule.kt`
- `feature/dashboard/domain/DashboardSummary.kt`
- `feature/dashboard/domain/DashboardRepository.kt`
- `feature/dashboard/data/RoomDashboardRepository.kt`
- `feature/dashboard/presentation/DashboardViewModel.kt`
- `feature/dashboard/presentation/DashboardScreen.kt`
- `app/build.gradle.kts`
- `CHANGELOG.md`

## Banco de dados
Nenhuma migração necessária. O Room permanece na versão 11.
