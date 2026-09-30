# Task List - Refazimento do CsvRosterParser

- [/] Commit e push inicial (antes das alterações de código)
- [ ] Atualizar `CsvRosterParser.kt` para suporte direto ao CSV original do PES 6 Editor (cabeçalho case-insensitive em EN/PT, delimitador automático `,` ou `;`, time em branco -> Banco da Liga)
- [ ] Atualizar `ImportCsvRostersUseCase.kt` para criação e associação automática de clubes sem depender de mapeamento manual
- [ ] Simplificar `CsvImportDialog.kt` e `SettingsViewModel.kt` para importação direta em 1 clique
- [ ] Atualizar e expandir os testes unitários em `CsvRosterParserTest.kt`
- [ ] Compilar e validar a aplicação (`Clean` -> `Rebuild` / `app:assembleDebug`)
- [ ] Criar walkthrough e executar commit e push final no GitHub com a mensagem `"Refazimento do CsvRosterParser: importação direta de CSV do PES 6 e criação automática de clubes"`
