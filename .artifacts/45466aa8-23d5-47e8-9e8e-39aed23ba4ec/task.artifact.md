# Task List - Importação de Elenco por Pasta (`ImportSquadFromFolderScreen`)

- [x] Commit e push inicial (antes das alterações de código)
- [x] Criar `Pes6PlayerTextParser.kt` (parser com suporte a arquivos mínimos/completos, atributos faltantes padrão 50 e Foot/Height nulos)
- [x] Atualizar `PlayerDao.kt`, `PlayerRepository.kt` e `RoomPlayerRepository.kt` (adicionar `deleteByClub` e verificação de partidas jogadas no clube)
- [x] Criar `ImportSquadFromFolderUseCase.kt` (verificação de partidas, transação atômica Room, leitura via SAF DocumentFile e relatório de importados/ignorados/erros)
- [x] Criar `ImportSquadFromFolderViewModel.kt` e `ImportSquadFromFolderScreen.kt` (interface Compose com seletor de clube, acionamento SAF `ACTION_OPEN_DOCUMENT_TREE`, progresso e relatório)
- [x] Integrar rota em `LegacyDestination.kt`, `LegacyNavGraph.kt` e adicionar o botão na aba Elenco de `ClubProfileScreen.kt`
- [x] Criar testes unitários em `ImportSquadFromFolderUseCaseTest.kt`
- [x] Compilar e validar a aplicação (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [x] Criar walkthrough e executar commit e push final no GitHub ("feat: importação de elenco por pasta TXT (SAF), parser PES6 e substituição atômica de elenco")
