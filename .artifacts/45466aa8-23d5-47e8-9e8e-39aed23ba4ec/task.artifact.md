# Task List - Campos Editáveis Foot e Height no Modelo de Jogador

- [/] Commit e push inicial (antes das alterações de código)
- [ ] Atualizar `AppDatabase.kt` (versão 31) e `DatabaseModule.kt` (migração v30->v31 no-op)
- [ ] Atualizar `PlayerDetailScreen.kt` e `PlayerDetailViewModel.kt` (`EditPlayerDialog` com seletor de Foot [R/L] e numérico de Height [100-250])
- [ ] Atualizar `ClubProfileScreen.kt` e `ClubProfileViewModel.kt` (`AddPlayerDialog` com padrões Foot: R e Height: 180)
- [ ] Atualizar `ExportPes6PlvrUseCase.kt` (preservar linhas `Foot:` e `Height:` quando nulos/vazios)
- [ ] Atualizar `ImportTeamAttributesParser.kt` (parser de Foot [R/L] e Height [100-250])
- [ ] Atualizar testes unitários (`ExportPes6PlvrUseCaseTest.kt` e `ImportTeamAttributesUseCaseTest.kt`)
- [ ] Compilar e validar a aplicação (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [ ] Criar walkthrough e executar commit e push final no GitHub ("feat: suporte aos campos editáveis Foot e Height no cadastro de jogador, migração v31 e exportador/parser PLVR")
