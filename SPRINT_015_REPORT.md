# Sprint 015 — Logs e Auditoria

## Objetivo
Implementar um histórico administrativo permanente e pesquisável para ações relevantes do Legacy Master Liga.

## Entregas
- Nova tabela Room `audit_logs`, banco atualizado da versão 7 para 8 e migração `7 -> 8`.
- Registro do usuário responsável com nome e perfil preservados no momento da ação.
- Categorias e ações auditadas: segurança, usuários, clubes, competições, temporadas, resultados, classificação, financeiro e mercado.
- Pesquisa textual e filtros por liga, categoria e ação.
- Tela Compose de Logs e Auditoria, protegida para Administradores.
- Atalho no Dashboard administrativo.
- Integração com Hilt, Room, Navigation Compose, Repositories e sessão ativa.
- Versão `0.15.0-alpha15`, `versionCode 15`.

## Regras de integridade
- Logs são somente de inclusão; nenhuma função de exclusão ou edição foi exposta.
- Nome e perfil do autor são armazenados como snapshot, preservando a auditoria mesmo após mudanças no usuário.
- A correção de placar registra uma nova ação e informa que a classificação foi recalculada.
- Movimentações financeiras e transferências registram valor, clubes envolvidos e entidade de origem.

## Arquivos criados
- `core/database/entity/AuditLogEntity.kt`
- `core/database/model/AuditLogRow.kt`
- `core/database/dao/AuditLogDao.kt`
- `feature/audit/domain/AuditModels.kt`
- `feature/audit/domain/AuditRepository.kt`
- `feature/audit/data/RoomAuditRepository.kt`
- `feature/audit/presentation/AuditViewModel.kt`
- `feature/audit/presentation/AuditScreen.kt`
- `SPRINT_015_REPORT.md`

## Arquivos modificados
- `core/database/AppDatabase.kt`
- `core/database/dao/SessionDao.kt`
- `core/di/DatabaseModule.kt`
- `core/di/RepositoryModule.kt`
- `core/navigation/LegacyDestination.kt`
- `core/navigation/LegacyNavGraph.kt`
- `feature/dashboard/presentation/DashboardScreen.kt`
- `feature/results/data/RoomResultsRepository.kt`
- `feature/finance/data/RoomFinanceRepository.kt`
- `feature/competitions/data/RoomCompetitionRepository.kt`
- `data/repository/RoomClubRepository.kt`
- `data/repository/RoomUserRepository.kt`
- `data/repository/RoomAuthRepository.kt`
- `app/build.gradle.kts`
- `CHANGELOG.md`

## Validação
- Estrutura do ZIP e referências internas verificadas.
- Migração, índices, bindings Hilt e rotas revisados estaticamente.
- A compilação automatizada foi iniciada, mas o Gradle Wrapper não pôde baixar a distribuição por indisponibilidade de DNS para `services.gradle.org` neste ambiente.
