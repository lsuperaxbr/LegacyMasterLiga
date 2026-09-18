# Sprint 016 — Backup e Restauração

## Objetivo

Proteger o histórico completo do Legacy Master Liga com backups locais verificáveis, exportação/importação pelo seletor de arquivos do Android, restauração segura e execução automática programada.

## Implementado

- Pacote próprio `.lmlbackup` contendo:
  - cópia consistente do banco Room/SQLite;
  - manifesto JSON com versão do formato, versão do aplicativo, versão do banco e data;
  - SHA-256 do banco para validação de integridade.
- Backup manual pela área administrativa.
- Backup automático diário ou semanal usando WorkManager, sem exigir internet.
- Retenção dos 10 backups automáticos mais recentes.
- Lista dos backups locais com data, tamanho, versão do app e versão do banco.
- Exportação para um arquivo escolhido pelo usuário via Storage Access Framework.
- Importação com validação antes de armazenar o pacote no aplicativo.
- Restauração com:
  - validação do formato, cabeçalho SQLite e SHA-256;
  - bloqueio de backups criados por uma versão de banco mais nova;
  - criação obrigatória de uma cópia de segurança antes de substituir os dados;
  - remoção dos arquivos WAL/SHM antigos;
  - reinício controlado do aplicativo após a troca do banco.
- Registro das ações de backup e restauração na Auditoria.
- Tela Compose protegida para Administradores.
- Atalho no Dashboard administrativo.

## Arquivos criados

- `feature/backup/domain/BackupModels.kt`
- `feature/backup/domain/BackupScheduler.kt`
- `feature/backup/data/BackupPackage.kt`
- `feature/backup/data/RoomBackupRepository.kt`
- `feature/backup/worker/BackupWorker.kt`
- `feature/backup/worker/WorkManagerBackupScheduler.kt`
- `feature/backup/presentation/BackupViewModel.kt`
- `feature/backup/presentation/BackupScreen.kt`
- `SPRINT_016_REPORT.md`

## Arquivos modificados

- `LegacyMasterLigaApp.kt`
- `core/di/RepositoryModule.kt`
- `core/navigation/LegacyDestination.kt`
- `core/navigation/LegacyNavGraph.kt`
- `feature/dashboard/presentation/DashboardScreen.kt`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `CHANGELOG.md`

## Banco de dados

Nenhuma tabela foi alterada. O Room permanece na versão 8. Os backups registram essa versão no manifesto e permitem que o Room aplique migrações existentes ao abrir um backup mais antigo.

## Validação

- Estrutura do pacote, rotas, bindings Hilt e referências verificadas estaticamente.
- ZIP final verificado com `unzip -t`.
- A compilação Gradle foi iniciada, mas o Wrapper não pôde ser baixado porque o ambiente não acessa `services.gradle.org`.
